package com.arthursouto.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetIndicationRequest(
        @NotBlank String indication
) {
}
