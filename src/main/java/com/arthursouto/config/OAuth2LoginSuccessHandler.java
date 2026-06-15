package com.arthursouto.config;

import com.arthursouto.domain.User;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend.redirect-url}")
    private String frontRedirectUrl;

    private String buildUsernameTemporary(String email, String googleId) {
        return email.split("@")[0] + "-" + googleId.substring(0, 4);
    }

    private User buildUserFromOAuth2(OAuth2User oAuth2User) {

        String id = Objects.requireNonNull(oAuth2User.getAttribute("sub"));
        String email = Objects.requireNonNull(oAuth2User.getAttribute("email"));
        String name = oAuth2User.getAttribute("name");

        return userRepository
                .findByGoogleId(id)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .googleId(id)
                                .email(email)
                                .name(name)
                                .username(buildUsernameTemporary(
                                        email,
                                        id
                                ))
                                .build()
                ));
    }

    private void redirectToFrontEnd(String token, @NonNull HttpServletResponse res) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromUriString(frontRedirectUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        res.sendRedirect(redirectUrl);
    }

    private void redirectToJson(String token, @NonNull HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.getWriter().write("{\"token\": \"" + token + "\"}");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        OAuth2User oAuth2User = ((OAuth2AuthenticationToken) auth).getPrincipal();
        User user = buildUserFromOAuth2(oAuth2User);
        String token = jwtService.generateToken(user);

        redirectToJson(token, res);
    }


}
