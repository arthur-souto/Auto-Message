package com.arthursouto.dto;

import com.arthursouto.domain.AssetIndication;

import java.time.Instant;
import java.util.UUID;

public record AssetIndicationResponse(
        UUID id,
        UUID assetId,
        String indication,
        Instant createdAt
) {
    public static AssetIndicationResponse from(AssetIndication indication) {
        return new AssetIndicationResponse(
                indication.getId(),
                indication.getAsset().getId(),
                indication.getIndication(),
                indication.getCreatedAt()
        );
    }
}
