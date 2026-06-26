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
        String manufacturer,
        String composition,
        String dosage,
        String mechanism,
        String associations,
        String pharmaForms,
        String literatureUrl,
        String category,
        boolean isExclusive,
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
                asset.getManufacturer(),
                asset.getComposition(),
                asset.getDosage(),
                asset.getMechanism(),
                asset.getAssociations(),
                asset.getPharmaForms(),
                asset.getLiteratureUrl(),
                asset.getCategory(),
                asset.isExclusive(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }
}
