package com.arthursouto.controller;

import com.arthursouto.config.AuthCodeCache;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthExchangeController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthExchangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthCodeCache authCodeCache;

    // JwtAuthFilter is picked up by @WebMvcTest's Filter-bean scan even though it's excluded
    // from the MockMvc chain via addFilters=false, so its constructor deps still need beans.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void exchangeReturnsTokensWhenCodeIsValid() throws Exception {
        when(authCodeCache.consume("valid-code"))
                .thenReturn(Optional.of(new AuthCodeCache.TokenPair("access-token", "refresh-token")));

        mockMvc.perform(post("/v1/api/auth/exchange")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthExchangeController.ExchangeRequest("valid-code"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void exchangeReturnsUnauthorizedWhenCodeIsInvalidOrExpired() throws Exception {
        when(authCodeCache.consume("bad-code")).thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/api/auth/exchange")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthExchangeController.ExchangeRequest("bad-code"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid code or expired"));
    }
}
