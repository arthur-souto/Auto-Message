package com.arthursouto.dto;

public record FormulaSignaturePrepareResponse(
        String sessionId,
        String dataToSignBase64
) {
}
