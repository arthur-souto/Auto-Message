package com.arthursouto.dto;

import com.arthursouto.domain.UserPlan;
import com.arthursouto.rules.PlanStatus;

import java.time.Instant;
import java.util.UUID;

public record UserPlanResponse(
        UUID id,
        PlanResponse plan,
        PlanStatus status,
        Instant startedAt,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserPlanResponse from(UserPlan userPlan) {
        return new UserPlanResponse(
                userPlan.getId(),
                PlanResponse.from(userPlan.getPlan()),
                userPlan.getStatus(),
                userPlan.getStartedAt(),
                userPlan.getExpiresAt(),
                userPlan.getCreatedAt(),
                userPlan.getUpdatedAt()
        );
    }
}
