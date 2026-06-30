package com.arthursouto.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record AssetUpdateRequest(
        String name,
        String supplier,
        String unit,
        String manufacturer,
        String composition,
        String dosage,
        String mechanism,
        String associations,
        String pharmaForms,
        String literatureUrl,
        String category,
        Boolean isExclusive,
        @DecimalMin("0.0") BigDecimal concentrationMin,
        @DecimalMin("0.0") BigDecimal concentrationMax,
        @DecimalMin("0.0") BigDecimal concentrationUsual,
        String concentrationUnit,
        String concentrationSource,
        String concentrationPharmaForm
) {
}
