package com.arthursouto.dto;

import java.util.List;

public record AssetImportResponse(
        int totalRows,
        int createdCount,
        int updatedCount,
        int failedCount,
        List<AssetImportRowError> errors
) {
    public static AssetImportResponse of(int totalRows, int createdCount, int updatedCount, List<AssetImportRowError> errors) {
        return new AssetImportResponse(totalRows, createdCount, updatedCount, errors.size(), errors);
    }
}
