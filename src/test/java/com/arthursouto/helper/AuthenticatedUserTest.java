package com.arthursouto.helper;

import com.arthursouto.domain.User;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.factory.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getReturnsAuthenticatedUserPrincipal() {
        User user = UserFactory.user();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        assertThat(AuthenticatedUser.get()).isEqualTo(user);
        assertThat(AuthenticatedUser.id()).isEqualTo(user.getId());
    }

    @Test
    void getThrowsWhenNoAuthenticationPresent() {
        assertThatThrownBy(AuthenticatedUser::get)
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getThrowsWhenPrincipalIsNotAUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-a-user", null)
        );

        assertThatThrownBy(AuthenticatedUser::get)
                .isInstanceOf(UnauthorizedException.class);
    }
}
