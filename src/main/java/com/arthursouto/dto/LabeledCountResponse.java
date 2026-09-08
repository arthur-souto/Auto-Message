package com.arthursouto.dto;

public record LabeledCountResponse(
        String label,
        long count
) {
}
