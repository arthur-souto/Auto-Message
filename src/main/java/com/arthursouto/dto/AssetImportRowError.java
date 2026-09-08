package com.arthursouto.dto;

public record AssetImportRowError(
        int rowNumber,
        String code,
        String reason
) {
}
