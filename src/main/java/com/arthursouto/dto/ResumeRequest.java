package com.arthursouto.dto;

import com.arthursouto.domain.Resume;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 10) String language,
        @Size(max = 50) String templateId,
        boolean isPrimary
) {
    public static Resume from(ResumeRequest req) {
        return Resume.builder()
                .title(req.title())
                .language(req.language() != null ? req.language() : null)
                .templateId(req.templateId() != null ? req.templateId() : null)
                .isPrimary(req.isPrimary())
                .build();
    }

}
