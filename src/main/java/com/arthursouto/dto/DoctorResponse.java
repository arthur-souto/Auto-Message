package com.arthursouto.dto;

import com.arthursouto.domain.Doctor;

import java.time.Instant;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        String name,
        String crm,
        String specialty,
        Instant createdAt,
        Instant updatedAt
) {
    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getCrm(),
                doctor.getSpecialty(),
                doctor.getCreatedAt(),
                doctor.getUpdatedAt()
        );
    }
}
