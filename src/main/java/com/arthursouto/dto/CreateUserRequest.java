package com.arthursouto.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String name,
        @NotBlank
        String username,
        @NotBlank
        @Size(min = 8, max = 72)
        String password,
        @AssertTrue(message = "You must accept the terms to register")
        boolean acceptedTerms
) {
}
