package com.arthursouto.dto;

import com.arthursouto.domain.Skill;

public record SkillResponse(
        Long id,
        Long resumeId,
        String name,
        String category,
        Short proficiency,
        short orderIndex
) {
    public static SkillResponse from(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getResume().getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getProficiency(),
                skill.getOrderIndex()
        );
    }
}
