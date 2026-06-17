package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.factory.UserFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final String REFRESH_SECRET = "fedcba9876543210fedcba9876543210fedcba9876543210";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000L, REFRESH_SECRET, 120_000L);
    }

    @Test
    void generatesAccessTokenWithExpectedClaims() {
        User user = UserFactory.user();

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.get("email")).isEqualTo(user.getEmail());
        assertThat(claims.get("username")).isEqualTo(user.getUsername());
    }

    @Test
    void generatesRefreshTokenWithExpectedClaims() {
        User user = UserFactory.user();

        String token = jwtService.generateRefreshToken(user);
        Claims claims = jwtService.parseRefreshClaims(token);

        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.get("email")).isEqualTo(user.getEmail());
    }

    @Test
    void rejectsAccessTokenParsedAsRefreshToken() {
        User user = UserFactory.user();
        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.parseRefreshClaims(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsRefreshTokenParsedAsAccessToken() {
        User user = UserFactory.user();
        String token = jwtService.generateRefreshToken(user);

        assertThatThrownBy(() -> jwtService.parseClaims(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsExpiredAccessToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L, REFRESH_SECRET, 120_000L);
        User user = UserFactory.user();
        String token = shortLived.generateToken(user);

        Thread.sleep(20);

        assertThatThrownBy(() -> shortLived.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
