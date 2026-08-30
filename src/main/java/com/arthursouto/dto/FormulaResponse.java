package com.arthursouto.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormulaResponse(
        UUID id,
        String name,
        String description,
        DoctorResponse doctor,
        List<FormulaItemResponse> items,
        List<IncompatibilityWarningResponse> incompatibilities,
        Instant createdAt,
        Instant updatedAt
) {
}
