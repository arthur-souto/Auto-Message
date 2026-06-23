package com.arthursouto.config;

import com.arthursouto.domain.User;
import com.arthursouto.issuer.RefreshTokenIssuer;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.JwtService;
import com.arthursouto.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthCodeCache authCodeCache;
    private final RefreshTokenIssuer refreshTokenIssuer;

    @Value("${app.frontend.redirect-url}")
    private String frontRedirectUrl;

    private static final int DURATION_SECONDS_CODE = 30;

    private String buildUsernameTemporary(String email, String googleId) {
        return email.split("@")[0] + "-" + googleId.substring(0, 4);
    }

    private User buildUserFromOAuth2(OAuth2User oAuth2User) {
        String id = Objects.requireNonNull(oAuth2User.getAttribute("sub"));
        String email = Objects.requireNonNull(oAuth2User.getAttribute("email"));
        String picture = oAuth2User.getAttribute("picture");
        String name = oAuth2User.getAttribute("name");

        return userRepository.findByGoogleId(id).orElseGet(() -> {

            final var user = userRepository.save(
                    User.builder()
                            .googleId(id)
                            .email(email)
                            .name(name)
                            .username(buildUsernameTemporary(email, id))
                            .profileImage(picture)
                            .build()
            );

            // future implement

            return user;
        });
    }

    private void redirect(String accessToken, String refreshToken, @NonNull HttpServletResponse res) throws IOException {
        var tokens = new AuthCodeCache.TokenPair(accessToken, refreshToken);
        var code = authCodeCache.generateCode(tokens, Duration.ofSeconds(DURATION_SECONDS_CODE));

        var redirectUrl = UriComponentsBuilder.fromUriString(frontRedirectUrl)
                .queryParam("code", code)
                .build()
                .toString();

        res.sendRedirect(redirectUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        OAuth2User oAuth2User = ((OAuth2AuthenticationToken) auth).getPrincipal();
        User user = buildUserFromOAuth2(oAuth2User);
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenIssuer.generate(user);

        redirect(accessToken, refreshToken, res);
    }

}
