package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.dto.UserUpdateRequest;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.exception.TooManyRequestsException;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private UserService userService;

    @Test
    void returnsMappedUserWhenFound() {
        User user = UserFactory.user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        MeResponse response = userService.getUserById(user.getId());

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.googleId()).isEqualTo(user.getGoogleId());
        assertThat(response.profileImage()).isEqualTo(user.getProfileImage());
    }

    @Test
    void throwsWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void activeUserThrowsWhenCodeIsWrong() {
        User user = UserFactory.user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(verificationCodeService.incrementAttempts(user.getId())).thenReturn(1);
        when(verificationCodeService.getCode(user.getId())).thenReturn(Optional.of("RIGHT1"));

        assertThatThrownBy(() -> userService.activeUser(user.getId(), "WRONG1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wrong code");

        assertThat(user.isVerified()).isFalse();
    }

    @Test
    void activeUserThrowsTooManyRequestsWhenAttemptsExceedLimit() {
        User user = UserFactory.user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(verificationCodeService.incrementAttempts(user.getId()))
                .thenReturn(VerificationCodeService.MAX_ATTEMPTS + 1);

        assertThatThrownBy(() -> userService.activeUser(user.getId(), "RIGHT1"))
                .isInstanceOf(TooManyRequestsException.class);

        verify(verificationCodeService, never()).getCode(user.getId());
    }

    @Test
    void activeUserActivatesAndClearsAttemptsWhenCodeMatches() {
        User user = UserFactory.user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(verificationCodeService.incrementAttempts(user.getId())).thenReturn(1);
        when(verificationCodeService.getCode(user.getId())).thenReturn(Optional.of("RIGHT1"));

        userService.activeUser(user.getId(), "RIGHT1");

        assertThat(user.isVerified()).isTrue();
        verify(userRepository).save(user);
        verify(verificationCodeService).invalidateCode(user.getId());
        verify(verificationCodeService).clearAttempts(user.getId());
    }

    @Test
    void activeUserThrowsWhenAccountIsDeleted() {
        User user = UserFactory.userBuilder().deletedAt(Instant.now()).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.activeUser(user.getId(), "RIGHT1"))
                .isInstanceOf(UnauthorizedException.class);

        verify(verificationCodeService, never()).incrementAttempts(user.getId());
    }

    @Test
    void startVerificationThrowsWhenAccountIsDeleted() {
        User user = UserFactory.userBuilder().deletedAt(Instant.now()).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.startVerification(user.getId()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateUserThrowsWhenAccountIsDeleted() {
        User user = UserFactory.userBuilder().deletedAt(Instant.now()).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUser(user.getId(), new UserUpdateRequest("New Name", null, null)))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, never()).save(user);
    }
}
