package com.arthursouto.dto;

import java.util.UUID;

public record AssetUsageResponse(
        UUID assetId,
        String assetName,
        long usageCount
) {
}
