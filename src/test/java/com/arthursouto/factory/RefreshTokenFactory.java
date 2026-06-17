package com.arthursouto.factory;

import com.arthursouto.domain.RefreshToken;
import com.arthursouto.domain.User;

import java.time.Instant;
import java.util.UUID;

public final class RefreshTokenFactory {

    private RefreshTokenFactory() {
    }

    public static RefreshToken.RefreshTokenBuilder builder(User owner) {
        Instant now = Instant.now();
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .tokenHash(UUID.randomUUID().toString())
                .user(owner)
                .createdAt(now)
                .expiresAt(now.plusSeconds(3600))
                .revoked(false);
    }

    public static RefreshToken validToken(User owner) {
        return builder(owner).build();
    }

    public static RefreshToken expiredToken(User owner) {
        Instant past = Instant.now().minusSeconds(3600);
        return builder(owner).expiresAt(past).build();
    }

    public static RefreshToken revokedToken(User owner) {
        return builder(owner).revoked(true).build();
    }
}
