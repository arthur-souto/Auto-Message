package com.arthursouto.dto;

import com.arthursouto.domain.AssetAssociation;

import java.time.Instant;
import java.util.UUID;

public record AssetAssociationResponse(
        UUID id,
        UUID assetId,
        String association,
        Instant createdAt
) {
    public static AssetAssociationResponse from(AssetAssociation association) {
        return new AssetAssociationResponse(
                association.getId(),
                association.getAsset().getId(),
                association.getAssociation(),
                association.getCreatedAt()
        );
    }
}
