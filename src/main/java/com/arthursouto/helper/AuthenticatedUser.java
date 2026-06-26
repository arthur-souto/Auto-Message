package com.arthursouto.helper;


import com.arthursouto.domain.User;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.repository.UserRepository;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public final class AuthenticatedUser {


    public static void isAccountVerified(UserRepository userRepository) {
        if (!userRepository.isVerifiedById(id())) {
            throw new UnauthorizedException("Account unverified");
        }
    }

    public static User isAccountVerifiedAndReturn(UserRepository userRepository) {
        User user = userRepository.findById(id())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isVerified()) {
            throw new UnauthorizedException("Account unverified");
        }

        return user;
    }

    public static UUID id() {
       var auth = SecurityContextHolder.getContext().getAuthentication();
       if(auth == null || !(auth.getPrincipal() instanceof  UUID userId)) {
           throw new UnauthorizedException("User not authenticated");
       }
       return userId;
    }
}
