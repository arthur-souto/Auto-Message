package com.arthursouto.dto;

import com.arthursouto.domain.Certification;

import java.time.LocalDate;

public record CertificationResponse(
        Long id,
        Long resumeId,
        String name,
        String issuingOrganization,
        LocalDate issueDate,
        LocalDate expirationDate,
        String credentialUrl,
        short orderIndex
) {
    public static CertificationResponse from(Certification certification) {
        return new CertificationResponse(
                certification.getId(),
                certification.getResume().getId(),
                certification.getName(),
                certification.getIssuingOrganization(),
                certification.getIssueDate(),
                certification.getExpirationDate(),
                certification.getCredentialUrl(),
                certification.getOrderIndex()
        );
    }
}
