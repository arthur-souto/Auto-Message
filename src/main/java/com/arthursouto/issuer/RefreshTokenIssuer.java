package com.arthursouto.issuer;

import com.arthursouto.domain.RefreshToken;
import com.arthursouto.domain.User;
import com.arthursouto.repository.RefreshTokenRepository;
import com.arthursouto.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RefreshTokenIssuer {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String generate(User user) {
        String token = jwtService.generateRefreshToken(user);
        Claims claims = jwtService.parseRefreshClaims(token);

        RefreshToken entity = RefreshToken.builder()
                .tokenHash(hash(token))
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(claims.getExpiration().toInstant())
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        return token;
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
