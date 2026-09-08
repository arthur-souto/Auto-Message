package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.CreateUserRequest;
import com.arthursouto.dto.LoginRequest;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.issuer.RefreshTokenIssuer;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenIssuer refreshTokenIssuer;
    @Mock
    private UserPlanService userPlanService;

    @InjectMocks
    private AuthService authService;

    @Test
    void createUserAccountSetsAcceptedTermsAtAndVersion() {
        ReflectionTestUtils.setField(authService, "termsVersion", "1.0");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(bCryptPasswordEncoder.encode("s3cret12")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenIssuer.generate(any(User.class))).thenReturn("refresh-token");

        authService.createUserAccount(new CreateUserRequest("new@example.com", "New User", "newuser", "s3cret12", true));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getAcceptedTermsAt()).isNotNull();
        assertThat(captor.getValue().getAcceptedTermsVersion()).isEqualTo("1.0");
    }

    @Test
    void loginThrowsWhenAccountIsDeleted() {
        User user = UserFactory.userBuilder().deletedAt(Instant.now()).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "any-password")))
                .isInstanceOf(UnauthorizedException.class);

        verify(bCryptPasswordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginReturnsTokensWhenPasswordMatches() {
        User user = UserFactory.userBuilder().password("hashed").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches("s3cret12", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenIssuer.generate(user)).thenReturn("refresh-token");

        var tokens = authService.login(new LoginRequest(user.getEmail(), "s3cret12"));

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-token");
    }
}
