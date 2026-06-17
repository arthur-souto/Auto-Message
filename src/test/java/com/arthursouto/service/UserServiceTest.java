package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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
}
