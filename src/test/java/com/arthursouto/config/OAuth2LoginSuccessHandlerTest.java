package com.arthursouto.config;

import com.arthursouto.domain.User;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.issuer.RefreshTokenIssuer;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.JwtService;
import com.arthursouto.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthCodeCache authCodeCache;
    @Mock
    private MessageService messageService;
    @Mock
    private RefreshTokenIssuer refreshTokenIssuer;
    @Mock
    private OAuth2User oAuth2User;

    private OAuth2LoginSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2LoginSuccessHandler(userRepository, jwtService, authCodeCache, messageService, refreshTokenIssuer);
        ReflectionTestUtils.setField(handler, "frontRedirectUrl", "https://app.example.com/callback");
    }

    @Test
    void redirectsWithCodeForExistingUser() throws Exception {
        User existing = UserFactory.userBuilder().googleId("google-42").build();
        when(oAuth2User.getAttribute("sub")).thenReturn("google-42");
        when(oAuth2User.getAttribute("email")).thenReturn(existing.getEmail());
        when(oAuth2User.getAttribute("name")).thenReturn(existing.getName());
        when(oAuth2User.getAttribute("picture")).thenReturn(existing.getProfileImage());
        when(userRepository.findByGoogleId("google-42")).thenReturn(Optional.of(existing));
        when(jwtService.generateToken(existing)).thenReturn("access-token");
        when(refreshTokenIssuer.generate(existing)).thenReturn("refresh-token");
        when(authCodeCache.generateCode(any(), any())).thenReturn("exchange-code");

        var auth = mock(OAuth2AuthenticationToken.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).startsWith("https://app.example.com/callback?code=exchange-code");
        verify(userRepository, never()).save(any());
        verify(messageService, never()).sendWelcomeMessage(any());
    }

    @Test
    void createsUserAndSendsWelcomeMessageForFirstTimeLogin() throws Exception {
        when(oAuth2User.getAttribute("sub")).thenReturn("google-99");
        when(oAuth2User.getAttribute("email")).thenReturn("new.user@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("New User");
        when(oAuth2User.getAttribute("picture")).thenReturn("https://example.com/avatar.png");
        when(userRepository.findByGoogleId("google-99")).thenReturn(Optional.empty());

        User saved = UserFactory.userBuilder()
                .googleId("google-99")
                .email("new.user@example.com")
                .name("New User")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("access-token");
        when(refreshTokenIssuer.generate(saved)).thenReturn("refresh-token");
        when(authCodeCache.generateCode(any(), any())).thenReturn("exchange-code");

        var auth = mock(OAuth2AuthenticationToken.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        verify(userRepository).save(any(User.class));
        verify(messageService).sendWelcomeMessage(saved);
        assertThat(response.getRedirectedUrl()).startsWith("https://app.example.com/callback?code=exchange-code");
    }
}
