package com.arthursouto.dto;

import com.arthursouto.domain.Education;

import java.time.LocalDate;

public record EducationResponse(
        Long id,
        Long resumeId,
        String institution,
        String degree,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        boolean isCurrent,
        String description,
        short orderIndex
) {
    public static EducationResponse from(Education education) {
        return new EducationResponse(
                education.getId(),
                education.getResume().getId(),
                education.getInstitution(),
                education.getDegree(),
                education.getFieldOfStudy(),
                education.getStartDate(),
                education.getEndDate(),
                education.isCurrent(),
                education.getDescription(),
                education.getOrderIndex()
        );
    }
}
