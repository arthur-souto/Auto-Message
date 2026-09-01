package com.arthursouto.dto;

import jakarta.validation.constraints.NotBlank;

public record FormulaSignatureFinishRequest(
        @NotBlank String sessionId,
        @NotBlank String signatureValueBase64
) {
}
