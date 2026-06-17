package com.arthursouto.issuer;

import com.arthursouto.domain.RefreshToken;
import com.arthursouto.domain.User;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.RefreshTokenRepository;
import com.arthursouto.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenIssuerTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private Claims claims;

    @InjectMocks
    private RefreshTokenIssuer refreshTokenIssuer;

    @Test
    void generatesAndPersistsRefreshTokenForUser() {
        User user = UserFactory.user();
        Date expiration = Date.from(Instant.now().plusSeconds(3600));

        when(jwtService.generateRefreshToken(user)).thenReturn("raw-refresh-token");
        when(jwtService.parseRefreshClaims("raw-refresh-token")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(expiration);

        String token = refreshTokenIssuer.generate(user);

        assertThat(token).isEqualTo("raw-refresh-token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiresAt()).isEqualTo(expiration.toInstant());
        assertThat(saved.getTokenHash()).isEqualTo(refreshTokenIssuer.hash("raw-refresh-token"));
    }

    @Test
    void hashIsDeterministicAndBase64Encoded() {
        String hash1 = refreshTokenIssuer.hash("same-token");
        String hash2 = refreshTokenIssuer.hash("same-token");

        assertThat(hash1).isEqualTo(hash2);
        assertThat(refreshTokenIssuer.hash("different-token")).isNotEqualTo(hash1);
    }
}
