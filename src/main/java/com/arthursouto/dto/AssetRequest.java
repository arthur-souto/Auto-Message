package com.arthursouto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AssetRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank String name,
        String supplier,
        @Size(max = 20) String unit,
        String manufacturer,
        String composition,
        String dosage,
        String mechanism,
        String pharmaForms,
        @Size(max = 500) String literatureUrl,
        String category,
        Boolean isExclusive,
        @DecimalMin("0.0") BigDecimal concentrationMin,
        @DecimalMin("0.0") BigDecimal concentrationMax,
        @DecimalMin("0.0") BigDecimal concentrationUsual,
        @Size(max = 20) String concentrationUnit,
        @Size(max = 100) String concentrationSource,
        @Size(max = 100) String concentrationPharmaForm
) {
}
