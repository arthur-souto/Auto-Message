package com.arthursouto.dto;

import com.arthursouto.domain.Asset;
import com.arthursouto.domain.FormulaItem;
import com.arthursouto.rules.ConcentrationChecker;
import com.arthursouto.rules.ConcentrationStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record FormulaItemResponse(
        UUID id,
        UUID assetId,
        String assetName,
        String assetCode,
        BigDecimal quantity,
        String unit,
        BigDecimal concentration,
        ConcentrationStatus concentrationStatus
) {
    public static FormulaItemResponse from(FormulaItem item) {
        Asset asset = item.getAsset();

        ConcentrationStatus status = item.getConcentration() == null
                ? null
                : ConcentrationChecker.classify(asset.getConcentrationMin(), asset.getConcentrationMax(), item.getConcentration());

        return new FormulaItemResponse(
                item.getId(),
                asset.getId(),
                asset.getName(),
                asset.getCode(),
                item.getQuantity(),
                item.getUnit(),
                item.getConcentration(),
                status
        );
    }
}
