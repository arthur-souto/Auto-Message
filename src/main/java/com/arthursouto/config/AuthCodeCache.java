package com.arthursouto.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record TokenPair(String accessToken, String refreshToken) {}

    private String buildRedisKey(String code) {
        return PREFIX + code;
    }

    public String generateCode(TokenPair tokens, Duration ttl) {
        var code = UUID.randomUUID().toString();

        try{
            String json = objectMapper.writeValueAsString(tokens);
            redisTemplate.opsForValue().set(buildRedisKey(code), json, ttl);
        }
        catch(JsonProcessingException e) {
            throw new IllegalStateException("Serializer failed", e);
        }
        return code;
    }

    public Optional<TokenPair> consume(String code) {
        String json = redisTemplate.opsForValue().getAndDelete(buildRedisKey(code));
        if(json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, TokenPair.class));
        }
        catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
