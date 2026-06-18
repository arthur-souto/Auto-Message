package com.arthursouto.dto;

import com.arthursouto.domain.Certification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record CertificationRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 150) String issuingOrganization,
        LocalDate issueDate,
        LocalDate expirationDate,
        @URL @Size(max = 500) String credentialUrl,
        Short orderIndex
) {
    public static Certification from(CertificationRequest req) {
        return Certification.builder()
                .name(req.name())
                .issuingOrganization(req.issuingOrganization())
                .issueDate(req.issueDate())
                .expirationDate(req.expirationDate())
                .credentialUrl(req.credentialUrl())
                .orderIndex(req.orderIndex() != null ? req.orderIndex() : 0)
                .build();
    }
}
