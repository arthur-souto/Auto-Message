package com.arthursouto.dto;

import com.arthursouto.domain.Project;

import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        Long resumeId,
        String name,
        String description,
        String url,
        String technologies,
        LocalDate startDate,
        LocalDate endDate,
        short orderIndex
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getResume().getId(),
                project.getName(),
                project.getDescription(),
                project.getUrl(),
                project.getTechnologies(),
                project.getStartDate(),
                project.getEndDate(),
                project.getOrderIndex()
        );
    }
}
