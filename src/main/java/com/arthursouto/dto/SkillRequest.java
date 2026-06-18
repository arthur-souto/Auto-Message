package com.arthursouto.dto;

import com.arthursouto.domain.Skill;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 50) String category,
        @Min(1) @Max(5) Short proficiency,
        Short orderIndex
) {
    public static Skill from(SkillRequest req) {
        return Skill.builder()
                .name(req.name())
                .category(req.category())
                .proficiency(req.proficiency())
                .orderIndex(req.orderIndex() != null ? req.orderIndex() : 0)
                .build();
    }
}
