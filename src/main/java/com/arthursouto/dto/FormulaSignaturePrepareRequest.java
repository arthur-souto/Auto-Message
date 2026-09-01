package com.arthursouto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FormulaSignaturePrepareRequest(
        @NotBlank String certificateBase64,
        @NotEmpty List<String> certificateChainBase64
) {
}
