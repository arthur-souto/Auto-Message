package com.arthursouto.dto;

import jakarta.validation.constraints.NotBlank;

public record DoctorRequest(
        @NotBlank String name,
        @NotBlank String crm,
        String specialty
) {
}
