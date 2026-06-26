package com.arthursouto.helper;

import com.arthursouto.domain.User;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null)
        );
    }

    @Test
    void idReturnsAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        authenticateAs(userId);

        assertThat(AuthenticatedUser.id()).isEqualTo(userId);
    }

    @Test
    void idThrowsWhenNoAuthenticationPresent() {
        assertThatThrownBy(AuthenticatedUser::id)
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void idThrowsWhenPrincipalIsNotUUID() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-a-uuid", null)
        );

        assertThatThrownBy(AuthenticatedUser::id)
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void isAccountVerifiedPassesWhenUserIsVerified() {
        UUID userId = UUID.randomUUID();
        authenticateAs(userId);
        when(userRepository.isVerifiedById(userId)).thenReturn(true);

        AuthenticatedUser.isAccountVerified(userRepository);
    }

    @Test
    void isAccountVerifiedThrowsWhenUserIsNotVerified() {
        UUID userId = UUID.randomUUID();
        authenticateAs(userId);
        when(userRepository.isVerifiedById(userId)).thenReturn(false);

        assertThatThrownBy(() -> AuthenticatedUser.isAccountVerified(userRepository))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account unverified");
    }

    @Test
    void isAccountVerifiedAndReturnReturnsVerifiedUser() {
        User user = UserFactory.userBuilder().isVerified(true).build();
        authenticateAs(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void isAccountVerifiedAndReturnThrowsWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        authenticateAs(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> AuthenticatedUser.isAccountVerifiedAndReturn(userRepository))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User not found");
    }

    @Test
    void isAccountVerifiedAndReturnThrowsWhenUserIsNotVerified() {
        User user = UserFactory.userBuilder().isVerified(false).build();
        authenticateAs(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> AuthenticatedUser.isAccountVerifiedAndReturn(userRepository))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account unverified");
    }
}
