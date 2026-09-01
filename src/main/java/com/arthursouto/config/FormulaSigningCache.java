package com.arthursouto.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Holds the state a formula-signing flow needs between the "prepare" and "finish" requests.
 * The same pattern as {@link AuthCodeCache}: a short-lived, single-use, Redis-backed blob keyed
 * by an opaque id (GETDEL on consume).
 */
@Component
@RequiredArgsConstructor
public class FormulaSigningCache {

    private static final String PREFIX = "formula:sign:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * {@code unsignedPdf} and {@code signingDateEpochMillis} MUST be reused byte-for-byte in the
     * "finish" step — recomputing either one (e.g. re-rendering the PDF, or using a fresh
     * {@code new Date()}) produces a different digest than the one the client actually signed.
     */
    public record SigningSession(
            UUID formulaId,
            UUID userId,
            byte[] unsignedPdf,
            byte[] signingCertificateDer,
            List<byte[]> certificateChainDer,
            long signingDateEpochMillis
    ) {
    }

    private String buildRedisKey(String sessionId) {
        return PREFIX + sessionId;
    }

    public String prepare(SigningSession session, Duration ttl) {
        var sessionId = UUID.randomUUID().toString();

        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(buildRedisKey(sessionId), json, ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Serializer failed", e);
        }

        return sessionId;
    }

    public Optional<SigningSession> consume(String sessionId) {
        String json = redisTemplate.opsForValue().getAndDelete(buildRedisKey(sessionId));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, SigningSession.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
