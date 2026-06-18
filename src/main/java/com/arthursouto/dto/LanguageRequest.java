package com.arthursouto.dto;

import com.arthursouto.domain.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LanguageRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull Language.Proficiency proficiency,
        Short orderIndex
) {
    public static Language from(LanguageRequest req) {
        return Language.builder()
                .name(req.name())
                .proficiency(req.proficiency())
                .orderIndex(req.orderIndex() != null ? req.orderIndex() : 0)
                .build();
    }
}
