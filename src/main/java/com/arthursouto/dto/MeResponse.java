package com.arthursouto.dto;

import com.arthursouto.domain.User;
import com.arthursouto.rules.Role;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String googleId,
        String email,
        String username,
        String profileImage,
        Role role,
        boolean isVerified
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getGoogleId(),
                user.getEmail(),
                user.getUsername(),
                user.getProfileImage(),
                user.getRole(),
                user.isVerified()
        );
    }
}
