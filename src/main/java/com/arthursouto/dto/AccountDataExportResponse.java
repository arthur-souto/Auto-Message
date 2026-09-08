package com.arthursouto.dto;

import java.time.Instant;
import java.util.List;

public record AccountDataExportResponse(
        MeResponse account,
        PharmacyProfileResponse pharmacyProfile,
        UserPlanResponse plan,
        List<PatientResponse> patients,
        List<DoctorResponse> doctors,
        List<FormulaResponse> formulas,
        List<AssetFavoriteResponse> favoriteAssets,
        Instant exportedAt
) {
}
