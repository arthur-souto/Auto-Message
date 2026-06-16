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

        RefreshToken saved = refreshTokenRepository.save(entity);
        log.warn("[DEBUG] generate() salvou id={} hash={} userId={}", saved.getId(), saved.getTokenHash(), user.getId());

        return token;
    }

    @Transactional
    public Optional<RotationResult> rotate(String oldToken) {
        log.warn("[DEBUG] rotate() chamado com token (primeiros 20 chars)={}",
                oldToken != null ? oldToken.substring(0, Math.min(20, oldToken.length())) : "null");

        try {
            jwtService.parseRefreshClaims(oldToken);
        } catch (JwtException e) {
            log.warn("[DEBUG] FALHA NO PARSE: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return Optional.empty();
        }

        String hashed = hash(oldToken);
        log.warn("[DEBUG] hash calculado para busca: {}", hashed);

        var found = refreshTokenRepository.findByTokenHashAndRevokedFalse(hashed);

        if (found.isEmpty()) {
            log.warn("[DEBUG] NENHUMA linha encontrada com esse hash (ou já revogada, ou nunca existiu)");
            return Optional.empty();
        }

        RefreshToken rt = found.get();
        log.warn("[DEBUG] encontrado id={} expiresAt={} now={}", rt.getId(), rt.getExpiresAt(), Instant.now());

        if (!rt.getExpiresAt().isAfter(Instant.now())) {
            log.warn("[DEBUG] TOKEN EXPIRADO segundo a coluna expires_at");
            return Optional.empty();
        }

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
        String newToken = generate(rt.getUser());
        log.warn("[DEBUG] rotação concluída com sucesso para userId={}", rt.getUser().getId());

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