package com.arthursouto.dto;

import com.arthursouto.domain.Patient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String name,
        String document,
        LocalDate birthDate,
        String phone,
        String email,
        String address,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getDocument(),
                patient.getBirthDate(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getNotes(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
