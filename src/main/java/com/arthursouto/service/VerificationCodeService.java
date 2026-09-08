package com.arthursouto.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final Duration EXPIRATION = Duration.ofMinutes(3);
    public static final int MAX_ATTEMPTS = 5;
    private final StringRedisTemplate redisTemplate;

    public Duration getExpiration() {
        return EXPIRATION;
    }

    public void save(UUID userId, String code) {
        final var key = buildKey(userId);
        redisTemplate.opsForValue().set(key, code, EXPIRATION);
    }

    public Optional<String> getCode(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(userId)));
    }

    public void invalidateCode(UUID userId) {
        redisTemplate.delete(buildKey(userId));
    }

    public int incrementAttempts(UUID userId) {
        String key = attemptsKey(userId);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, EXPIRATION);
        }
        return count == null ? 1 : count.intValue();
    }

    public void clearAttempts(UUID userId) {
        redisTemplate.delete(attemptsKey(userId));
    }

    private String buildKey(UUID usuarioId) {
        return "verification:" + usuarioId;
    }

    private String attemptsKey(UUID userId) {
        return "verification:attempts:" + userId;
    }
}
