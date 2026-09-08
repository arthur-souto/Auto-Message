package com.arthursouto.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long totalAssets,
        long totalFormulas,
        long totalDoctors,
        long totalFavorites,
        long formulasWithIncompatibilityWarnings,
        List<LabeledCountResponse> assetsByCategory,
        List<LabeledCountResponse> concentrationStatusBreakdown,
        List<LabeledCountResponse> formulasCreatedByMonth,
        List<AssetUsageResponse> topAssetsUsedInFormulas
) {
}
