package com.arthursouto.controller;

import com.arthursouto.domain.User;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.JwtService;
import com.arthursouto.service.RefreshTokenService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    void getMeReturnsAuthenticatedUserDetails() throws Exception {
        User user = UserFactory.user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        mockMvc.perform(get("/v1/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }
}
