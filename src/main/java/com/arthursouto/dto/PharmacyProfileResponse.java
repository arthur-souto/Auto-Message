package com.arthursouto.dto;

import com.arthursouto.domain.PharmacyProfile;

import java.time.Instant;
import java.util.UUID;

public record PharmacyProfileResponse(
        UUID id,
        String pharmacyName,
        String address,
        String phone,
        String email,
        String responsibleName,
        String responsibleDocument,
        String responsibleRegistration,
        Instant createdAt,
        Instant updatedAt
) {
    public static PharmacyProfileResponse from(PharmacyProfile profile) {
        return new PharmacyProfileResponse(
                profile.getId(),
                profile.getPharmacyName(),
                profile.getAddress(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getResponsibleName(),
                profile.getResponsibleDocument(),
                profile.getResponsibleRegistration(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
