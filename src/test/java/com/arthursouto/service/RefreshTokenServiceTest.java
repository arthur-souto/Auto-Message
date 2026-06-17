package com.arthursouto.service;

import com.arthursouto.domain.RefreshToken;
import com.arthursouto.domain.User;
import com.arthursouto.factory.RefreshTokenFactory;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.issuer.RefreshTokenIssuer;
import com.arthursouto.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenIssuer refreshTokenIssuer;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void rotateReturnsEmptyWhenOldTokenFailsJwtValidation() {
        when(jwtService.parseRefreshClaims("bad-token")).thenThrow(new JwtException("invalid"));

        Optional<RefreshTokenService.RotationResult> result = refreshTokenService.rotate("bad-token");

        assertThat(result).isEmpty();
        verifyNoInteractions(refreshTokenRepository, refreshTokenIssuer);
    }

    @Test
    void rotateReturnsEmptyWhenTokenHashNotFound() {
        when(refreshTokenIssuer.hash("token")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse("hash")).thenReturn(Optional.empty());

        Optional<RefreshTokenService.RotationResult> result = refreshTokenService.rotate("token");

        assertThat(result).isEmpty();
    }

    @Test
    void rotateReturnsEmptyWhenTokenExpired() {
        User user = UserFactory.user();
        RefreshToken expired = RefreshTokenFactory.expiredToken(user);
        when(refreshTokenIssuer.hash("token")).thenReturn(expired.getTokenHash());
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(expired.getTokenHash()))
                .thenReturn(Optional.of(expired));

        Optional<RefreshTokenService.RotationResult> result = refreshTokenService.rotate("token");

        assertThat(result).isEmpty();
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotateRevokesOldTokenAndIssuesNewOneOnSuccess() {
        User user = UserFactory.user();
        RefreshToken valid = RefreshTokenFactory.validToken(user);
        when(refreshTokenIssuer.hash("token")).thenReturn(valid.getTokenHash());
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(valid.getTokenHash()))
                .thenReturn(Optional.of(valid));
        when(refreshTokenIssuer.generate(user)).thenReturn("new-refresh-token");

        Optional<RefreshTokenService.RotationResult> result = refreshTokenService.rotate("token");

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(user.getId());
        assertThat(result.get().newToken()).isEqualTo("new-refresh-token");
        assertThat(valid.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(valid);
    }

    @Test
    void revokeMarksMatchingTokenAsRevoked() {
        User user = UserFactory.user();
        RefreshToken valid = RefreshTokenFactory.validToken(user);
        when(refreshTokenIssuer.hash("token")).thenReturn(valid.getTokenHash());
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse(valid.getTokenHash()))
                .thenReturn(Optional.of(valid));

        refreshTokenService.revoke("token");

        assertThat(valid.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(valid);
    }

    @Test
    void revokeDoesNothingWhenTokenNotFound() {
        when(refreshTokenIssuer.hash("token")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHashAndRevokedFalse("hash")).thenReturn(Optional.empty());

        refreshTokenService.revoke("token");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void revokeAllDelegatesToRepository() {
        UUID userId = UUID.randomUUID();

        refreshTokenService.revokeAll(userId);

        verify(refreshTokenRepository).revokeAllForUser(userId);
    }

    @Test
    void cleanupExpiredDeletesTokensExpiredBeforeNow() {
        refreshTokenService.cleanupExpired();

        verify(refreshTokenRepository).deleteByExpiresAtBefore(any(Instant.class));
    }
}
