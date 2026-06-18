package com.arthursouto.dto;

import com.arthursouto.domain.WorkExperience;

import java.time.LocalDate;

public record WorkExperienceResponse(
        Long id,
        Long resumeId,
        String company,
        String position,
        String location,
        String employmentType,
        LocalDate startDate,
        LocalDate endDate,
        boolean isCurrent,
        String description,
        short orderIndex
) {
    public static WorkExperienceResponse from(WorkExperience workExperience) {
        return new WorkExperienceResponse(
                workExperience.getId(),
                workExperience.getResume().getId(),
                workExperience.getCompany(),
                workExperience.getPosition(),
                workExperience.getLocation(),
                workExperience.getEmploymentType(),
                workExperience.getStartDate(),
                workExperience.getEndDate(),
                workExperience.isCurrent(),
                workExperience.getDescription(),
                workExperience.getOrderIndex()
        );
    }
}
