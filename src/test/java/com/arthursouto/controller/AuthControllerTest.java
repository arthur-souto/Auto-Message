package com.arthursouto.controller;

import com.arthursouto.domain.User;
import com.arthursouto.dto.AccountDataExportResponse;
import com.arthursouto.dto.CreateUserRequest;
import com.arthursouto.dto.LoginRequest;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.dto.TokenPairResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.AccountDataService;
import com.arthursouto.service.AuthService;
import com.arthursouto.service.JwtService;
import com.arthursouto.service.RefreshTokenService;
import com.arthursouto.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RefreshTokenService refreshTokenService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private AccountDataService accountDataService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void refreshReturnsNewTokensWhenRotationSucceeds() throws Exception {
        User user = UserFactory.user();
        var rotation = new RefreshTokenService.RotationResult(user.getId(), "new-refresh-token");
        when(refreshTokenService.rotate("old-token")).thenReturn(Optional.of(rotation));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("new-access-token");

        mockMvc.perform(post("/v1/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthController.RefreshRequest("old-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refreshReturnsUnauthorizedWhenRotationFails() throws Exception {
        when(refreshTokenService.rotate("bad-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthController.RefreshRequest("bad-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh token invalid or expired"));
    }

    @Test
    void refreshThrowsWhenRotatedUserNoLongerExists() throws Exception {
        var userId = java.util.UUID.randomUUID();
        var rotation = new RefreshTokenService.RotationResult(userId, "new-refresh-token");
        when(refreshTokenService.rotate("old-token")).thenReturn(Optional.of(rotation));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthController.RefreshRequest("old-token"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void logoutRevokesTokenAndReturnsNoContent() throws Exception {
        mockMvc.perform(post("/v1/api/auth/logout")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthController.RefreshRequest("some-token"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutAllRevokesEverySessionForAuthenticatedUserAndReturnsNoContent() throws Exception {
        User user = UserFactory.user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null)
        );

        mockMvc.perform(post("/v1/api/auth/logout-all"))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).revokeAll(user.getId());
    }

    @Test
    void registerReturnsCreatedTokenPair() throws Exception {
        var request = new CreateUserRequest("new@example.com", "New User", "newuser", "s3cret12", true);
        var tokens = new TokenPairResponse("access-token", "refresh-token");
        when(authService.createUserAccount(request)).thenReturn(tokens);

        mockMvc.perform(post("/v1/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void loginReturnsTokenPair() throws Exception {
        var request = new LoginRequest("user@example.com", "s3cret");
        var tokens = new TokenPairResponse("access-token", "refresh-token");
        when(authService.login(request)).thenReturn(tokens);

        mockMvc.perform(post("/v1/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void getMeReturnsAuthenticatedUserDetails() throws Exception {
        User user = UserFactory.user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null)
        );
        when(userService.getUserById(user.getId())).thenReturn(MeResponse.from(user));

        mockMvc.perform(get("/v1/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    void deleteMeAnonymizesAccountAndReturnsNoContent() throws Exception {
        User user = UserFactory.user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null)
        );

        mockMvc.perform(delete("/v1/api/auth/me"))
                .andExpect(status().isNoContent());

        verify(accountDataService).deleteMyAccount(user.getId());
    }

    @Test
    void exportMyDataReturnsAccountDataExport() throws Exception {
        User user = UserFactory.user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null)
        );
        var export = new AccountDataExportResponse(
                MeResponse.from(user), null, null, List.of(), List.of(), List.of(), List.of(), Instant.now()
        );
        when(accountDataService.exportMyData(user.getId())).thenReturn(export);

        mockMvc.perform(get("/v1/api/auth/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.id").value(user.getId().toString()));
    }
}
