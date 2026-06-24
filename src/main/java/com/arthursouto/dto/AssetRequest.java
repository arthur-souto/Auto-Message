package com.arthursouto.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetRequest(
        @NotBlank String code,
        @NotBlank String name,
        String supplier,
        String unit
) {
}
