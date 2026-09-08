package com.arthursouto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FormulaItemRequest(
        @NotNull UUID assetId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        @NotBlank String unit,
        @DecimalMin("0.0") BigDecimal concentration
) {
}
