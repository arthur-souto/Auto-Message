package com.arthursouto.dto;

public record PharmacyProfileRequest(
        String pharmacyName,
        String address,
        String phone,
        String email,
        String responsibleName,
        String responsibleDocument,
        String responsibleRegistration
) {
}
