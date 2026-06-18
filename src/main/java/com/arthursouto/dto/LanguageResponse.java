package com.arthursouto.dto;

import com.arthursouto.domain.Language;

public record LanguageResponse(
        Long id,
        Long resumeId,
        String name,
        Language.Proficiency proficiency,
        short orderIndex
) {
    public static LanguageResponse from(Language language) {
        return new LanguageResponse(
                language.getId(),
                language.getResume().getId(),
                language.getName(),
                language.getProficiency(),
                language.getOrderIndex()
        );
    }
}
