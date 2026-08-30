package com.arthursouto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssetIncompatibilityRequest(
        @NotNull UUID otherAssetId,
        @NotBlank String reason
) {
}
