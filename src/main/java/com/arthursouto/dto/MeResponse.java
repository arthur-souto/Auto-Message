package com.arthursouto.dto;

import com.arthursouto.domain.User;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String googleId,
        String email,
        String username,
        String profileImage
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getGoogleId(),
                user.getEmail(),
                user.getUsername(),
                user.getProfileImage()
        );
    }
}
