package com.arthursouto.dto;

import java.util.UUID;

public record IncompatibilityWarningResponse(
        UUID assetAId,
        String assetAName,
        UUID assetBId,
        String assetBName,
        String reason
) {
}
