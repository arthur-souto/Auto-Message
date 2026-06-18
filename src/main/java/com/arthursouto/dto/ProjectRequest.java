package com.arthursouto.dto;

import com.arthursouto.domain.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        @URL @Size(max = 500) String url,
        @Size(max = 500) String technologies,
        LocalDate startDate,
        LocalDate endDate,
        Short orderIndex
) {
    public static Project from(ProjectRequest req) {
        return Project.builder()
                .name(req.name())
                .description(req.description())
                .url(req.url())
                .technologies(req.technologies())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .orderIndex(req.orderIndex() != null ? req.orderIndex() : 0)
                .build();
    }
}
