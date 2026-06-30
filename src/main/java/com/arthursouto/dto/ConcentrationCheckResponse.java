package com.arthursouto.dto;

import com.arthursouto.rules.ConcentrationStatus;

import java.math.BigDecimal;

public record ConcentrationCheckResponse(
        BigDecimal value,
        ConcentrationStatus status,
        BigDecimal concentrationMin,
        BigDecimal concentrationMax,
        BigDecimal concentrationUsual,
        String concentrationUnit,
        String concentrationSource,
        String concentrationPharmaForm
) {
}
