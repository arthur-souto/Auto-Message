package com.arthursouto.dto;

import java.time.Instant;
import java.util.UUID;

public record AssetIncompatibilityResponse(
        UUID id,
        UUID assetId,
        String assetName,
        String reason,
        Instant createdAt
) {
}
