package com.arthursouto.helper;


import com.arthursouto.domain.User;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.repository.UserRepository;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public final class AuthenticatedUser {


    public static User user(UserRepository userRepository) {
        return userRepository.findById(id())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public static UUID id() {
       var auth = SecurityContextHolder.getContext().getAuthentication();
       if(auth == null || !(auth.getPrincipal() instanceof  UUID userId)) {
           throw new UnauthorizedException("User not authenticated");
       }
       return userId;
    }
}
