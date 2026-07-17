package com.arthursouto.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 1, max = 255) String name,
        @Size(min = 1, max = 255) String username,
        String profileImage
) {
}
