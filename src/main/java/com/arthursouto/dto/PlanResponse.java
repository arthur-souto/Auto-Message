package com.arthursouto.dto;

import com.arthursouto.domain.Plan;
import com.arthursouto.rules.PlanType;

import java.util.UUID;

public record PlanResponse(
        UUID id,
        PlanType type,
        String name,
        Integer maxFormulas,
        Integer maxAssets
) {
    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getType(),
                plan.getName(),
                plan.getMaxFormulas(),
                plan.getMaxAssets()
        );
    }
}
