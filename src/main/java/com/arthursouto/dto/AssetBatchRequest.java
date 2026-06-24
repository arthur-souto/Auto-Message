package com.arthursouto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetBatchRequest(
        String batch,
        LocalDate expiresAt,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal total
) {
}
