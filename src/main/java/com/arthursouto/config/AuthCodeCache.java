package com.arthursouto.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthCodeCache {

    private static final String PREFIX = "auth:code:";
    private final StringRedisTemplate redisTemplate;


    private String buildRedisKey(String code) {
        return PREFIX + code;
    }

    public String generateCode(String token, Duration ttl) {
        var code = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(buildRedisKey(code), token, ttl);
        return code;
    }

    public Optional<String> consume(String code) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(buildRedisKey(code)));
    }
}
