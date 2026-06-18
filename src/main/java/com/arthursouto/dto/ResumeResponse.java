package com.arthursouto.dto;

import com.arthursouto.domain.Resume;

import java.time.Instant;

public record ResumeResponse(
        Long id,
        String title,
        String language,
        String templateId,
        boolean isPrimary,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getLanguage(),
                resume.getTemplateId(),
                resume.isPrimary(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }
}
