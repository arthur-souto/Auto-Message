package com.arthursouto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record FormulaRequest(
        @NotBlank String name,
        String description,
        UUID doctorId,
        @NotEmpty @Valid List<FormulaItemRequest> items
) {
}
