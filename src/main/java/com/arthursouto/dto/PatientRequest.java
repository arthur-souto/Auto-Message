package com.arthursouto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank String name,
        @NotBlank String document,
        @Past LocalDate birthDate,
        String phone,
        @Email String email,
        String address,
        String notes
) {
}
