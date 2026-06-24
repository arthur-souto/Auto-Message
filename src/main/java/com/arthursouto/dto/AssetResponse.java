package com.arthursouto.dto;

import com.arthursouto.domain.Asset;

import java.time.Instant;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String code,
        String name,
        String supplier,
        String unit,
        Instant createdAt,
        Instant updatedAt
) {
    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getCode(),
                asset.getName(),
                asset.getSupplier(),
                asset.getUnit(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }
}
