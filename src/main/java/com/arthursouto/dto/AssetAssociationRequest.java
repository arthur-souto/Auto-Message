package com.arthursouto.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetAssociationRequest(
        @NotBlank String association
) {
}
