package com.arthursouto.service;

import com.arthursouto.dto.AssetRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

record RawAssetRow(
        int rowNumber,
        String rawCode,
        String parseError,
        AssetRequest assetRequest
) {

    static final List<String> COLUMNS = List.of(
            "code", "name", "supplier", "unit", "manufacturer", "composition", "dosage",
            "mechanism", "pharma_forms", "literature_url", "category", "is_exclusive",
            "concentration_min", "concentration_max", "concentration_usual",
            "concentration_unit", "concentration_source", "concentration_pharma_form"
    );

    boolean hasParseError() {
        return parseError != null;
    }

    static RawAssetRow error(int rowNumber, String rawCode, String reason) {
        return new RawAssetRow(rowNumber, rawCode, reason, null);
    }

    static RawAssetRow from(int rowNumber, Map<String, String> cells) {
        String code = trimToNull(cells.get("code"));
        if (code == null) {
            return error(rowNumber, null, "code is required");
        }

        String name = trimToNull(cells.get("name"));
        if (name == null) {
            return error(rowNumber, code, "name is required");
        }

        try {
            AssetRequest request = new AssetRequest(
                    code,
                    name,
                    trimToNull(cells.get("supplier")),
                    trimToNull(cells.get("unit")),
                    trimToNull(cells.get("manufacturer")),
                    trimToNull(cells.get("composition")),
                    trimToNull(cells.get("dosage")),
                    trimToNull(cells.get("mechanism")),
                    trimToNull(cells.get("pharma_forms")),
                    trimToNull(cells.get("literature_url")),
                    trimToNull(cells.get("category")),
                    parseBoolean(cells.get("is_exclusive")),
                    parseDecimal("concentration_min", cells.get("concentration_min")),
                    parseDecimal("concentration_max", cells.get("concentration_max")),
                    parseDecimal("concentration_usual", cells.get("concentration_usual")),
                    trimToNull(cells.get("concentration_unit")),
                    trimToNull(cells.get("concentration_source")),
                    trimToNull(cells.get("concentration_pharma_form"))
            );
            return new RawAssetRow(rowNumber, code, null, request);
        } catch (IllegalArgumentException e) {
            return error(rowNumber, code, e.getMessage());
        }
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean parseBoolean(String raw) {
        String value = trimToNull(raw);
        if (value == null) return false;
        return switch (value.toLowerCase()) {
            case "true", "1", "yes", "sim" -> true;
            case "false", "0", "no", "não", "nao" -> false;
            default -> throw new IllegalArgumentException("is_exclusive: invalid boolean value '" + raw + "'");
        };
    }

    private static BigDecimal parseDecimal(String field, String raw) {
        String value = trimToNull(raw);
        if (value == null) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + ": invalid decimal value '" + raw + "'");
        }
    }
}
