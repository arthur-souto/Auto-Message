package com.arthursouto.config;

import com.arthursouto.dto.TokenPairResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCodeCacheTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthCodeCache authCodeCache;

    @Test
    void generateCodeStoresSerializedTokensUnderPrefixedKey() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        var tokens = new TokenPairResponse("access-token", "refresh-token");

        String code = authCodeCache.generateCode(tokens, Duration.ofSeconds(30));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofSeconds(30)));

        assertThat(keyCaptor.getValue()).isEqualTo("auth:code:" + code);
        assertThat(valueCaptor.getValue()).contains("access-token").contains("refresh-token");

        // Round-trips the exact stored JSON back through the real deserializer to
        // rule out a field swap between accessToken/refreshToken during (de)serialization.
        TokenPairResponse roundTripped = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(valueCaptor.getValue(), TokenPairResponse.class);
        assertThat(roundTripped.accessToken()).isEqualTo("access-token");
        assertThat(roundTripped.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void consumeReturnsTokenPairWhenCodeExistsAndDeletesIt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String code = "abc-123";
        when(valueOperations.getAndDelete("auth:code:" + code))
                .thenReturn("{\"accessToken\":\"a\",\"refreshToken\":\"r\"}");

        Optional<TokenPairResponse> result = authCodeCache.consume(code);

        assertThat(result).contains(new TokenPairResponse("a", "r"));
    }

    @Test
    void consumeReturnsEmptyWhenCodeMissing() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(any())).thenReturn(null);

        Optional<TokenPairResponse> result = authCodeCache.consume("missing-code");

        assertThat(result).isEmpty();
    }

    @Test
    void consumeReturnsEmptyWhenStoredJsonIsMalformed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(any())).thenReturn("not-json");

        Optional<TokenPairResponse> result = authCodeCache.consume("bad-code");

        assertThat(result).isEmpty();
    }
}
