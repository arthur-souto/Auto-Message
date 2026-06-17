package com.arthursouto.service;

import com.arthursouto.domain.RefreshToken;
import com.arthursouto.domain.User;
import com.arthursouto.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;


    public record RotationResult(UUID userId, String newToken) {}

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

    @Transactional
    public Optional<RotationResult> rotate(String oldToken) {
        try {
            jwtService.parseRefreshClaims(oldToken);
        } catch (JwtException e) {
            return Optional.empty();
        }

        String hashed = hash(oldToken);

        var found = refreshTokenRepository.findByTokenHashAndRevokedFalse(hashed);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken rt = found.get();

        if (!rt.getExpiresAt().isAfter(Instant.now())) {
            return Optional.empty();
        }

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
        String newToken = generate(rt.getUser());

        return Optional.of(new RotationResult(rt.getUser().getId(), newToken));
    }

    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(token))
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Transactional
    public void revokeAll(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpired() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}