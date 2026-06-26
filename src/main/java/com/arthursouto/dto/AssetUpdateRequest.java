package com.arthursouto.dto;

public record AssetUpdateRequest(
        String name,
        String supplier,
        String unit,
        String manufacturer,
        String composition,
        String dosage,
        String mechanism,
        String associations,
        String pharmaForms,
        String literatureUrl,
        String category,
        Boolean isExclusive
) {
}
