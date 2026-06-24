package com.arthursouto.dto;

import com.arthursouto.domain.AssetBatch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetBatchResponse(
        UUID id,
        UUID assetId,
        String batch,
        LocalDate expiresAt,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal total,
        Instant createdAt
) {
    public static AssetBatchResponse from(AssetBatch assetBatch) {
        return new AssetBatchResponse(
                assetBatch.getId(),
                assetBatch.getAsset().getId(),
                assetBatch.getBatch(),
                assetBatch.getExpiresAt(),
                assetBatch.getQuantity(),
                assetBatch.getUnitCost(),
                assetBatch.getTotal(),
                assetBatch.getCreatedAt()
        );
    }
}
