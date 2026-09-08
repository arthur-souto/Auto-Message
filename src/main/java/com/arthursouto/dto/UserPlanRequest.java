package com.arthursouto.dto;

import com.arthursouto.rules.PlanType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UserPlanRequest(
        @NotNull PlanType planType,
        Instant expiresAt
) {
}
