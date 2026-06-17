package com.arthursouto.config;

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
    void generateCodeStoresSerializedTokensUnderPrefixedKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        var tokens = new AuthCodeCache.TokenPair("access-token", "refresh-token");

        String code = authCodeCache.generateCode(tokens, Duration.ofSeconds(30));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofSeconds(30)));

        assertThat(keyCaptor.getValue()).isEqualTo("auth:code:" + code);
        assertThat(valueCaptor.getValue()).contains("access-token").contains("refresh-token");
    }

    @Test
    void consumeReturnsTokenPairWhenCodeExistsAndDeletesIt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String code = "abc-123";
        when(valueOperations.getAndDelete("auth:code:" + code))
                .thenReturn("{\"accessToken\":\"a\",\"refreshToken\":\"r\"}");

        Optional<AuthCodeCache.TokenPair> result = authCodeCache.consume(code);

        assertThat(result).contains(new AuthCodeCache.TokenPair("a", "r"));
    }

    @Test
    void consumeReturnsEmptyWhenCodeMissing() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(any())).thenReturn(null);

        Optional<AuthCodeCache.TokenPair> result = authCodeCache.consume("missing-code");

        assertThat(result).isEmpty();
    }

    @Test
    void consumeReturnsEmptyWhenStoredJsonIsMalformed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(any())).thenReturn("not-json");

        Optional<AuthCodeCache.TokenPair> result = authCodeCache.consume("bad-code");

        assertThat(result).isEmpty();
    }
}
