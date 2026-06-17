package com.arthursouto.helper;


import com.arthursouto.domain.User;
import com.arthursouto.exception.UnauthorizedException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public final class AuthenticatedUser {

    public static User get() {
        var p = SecurityContextHolder.getContext().getAuthentication();
        if(p == null || !(p.getPrincipal() instanceof User user)) {
            throw new UnauthorizedException("User not authenticated");
        }
        return user;
    }

    public static UUID id() {
        return get().getId();
    }
}
