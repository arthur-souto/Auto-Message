package com.arthursouto.dto;

import com.arthursouto.domain.WorkExperience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record WorkExperienceRequest(
        @NotBlank @Size(max = 150) String company,
        @NotBlank @Size(max = 150) String position,
        @Size(max = 150) String location,
        @Size(max = 50) String employmentType,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        boolean isCurrent,
        String description,
        Short orderIndex
) {
    public static WorkExperience from(WorkExperienceRequest req) {
        return WorkExperience.builder()
                .company(req.company())
                .position(req.position())
                .location(req.location())
                .employmentType(req.employmentType())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .isCurrent(req.isCurrent())
                .description(req.description())
                .orderIndex(req.orderIndex() != null ? req.orderIndex() : 0)
                .build();
    }
}
