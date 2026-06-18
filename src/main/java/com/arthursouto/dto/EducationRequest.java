package com.arthursouto.dto;

import com.arthursouto.domain.Education;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EducationRequest(
        @NotBlank @Size(max = 150) String institution,
        @Size(max = 100) String degree,
        @Size(max = 150) String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        boolean isCurrent,
        String description,
        Short orderIndex
) {
    public static Education from(EducationRequest req) {
        return Education.builder()
                .institution(req.institution())
                .degree(req.degree())
                .fieldOfStudy(req.fieldOfStudy())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .isCurrent(req.isCurrent())
                .description(req.description())
                .orderIndex(req.orderIndex() != null ? req.orderIndex() : 0)
                .build();
    }
}
