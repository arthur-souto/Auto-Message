package com.arthursouto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String name,
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
