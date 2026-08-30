package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.AssetUsageResponse;
import com.arthursouto.dto.DashboardSummaryResponse;
import com.arthursouto.dto.LabeledCountResponse;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.AssetFavoriteRepository;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.DoctorRepository;
import com.arthursouto.repository.FormulaItemRepository;
import com.arthursouto.repository.FormulaRepository;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int TOP_ASSETS_LIMIT = 5;

    private final AssetRepository assetRepository;
    private final FormulaRepository formulaRepository;
    private final FormulaItemRepository formulaItemRepository;
    private final DoctorRepository doctorRepository;
    private final AssetFavoriteRepository assetFavoriteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        UUID userId = user.getId();

        return new DashboardSummaryResponse(
                assetRepository.count(),
                formulaRepository.countByUserId(userId),
                doctorRepository.countByUserId(userId),
                assetFavoriteRepository.countByUserId(userId),
                formulaRepository.countFormulasWithIncompatibilities(userId),
                toLabeledCounts(assetRepository.countByCategory()),
                toLabeledCounts(formulaItemRepository.countByConcentrationStatus(userId)),
                toLabeledCounts(formulaRepository.countByCreationMonth(userId)),
                toAssetUsage(formulaItemRepository.topAssetsUsedInFormulas(userId, PageRequest.of(0, TOP_ASSETS_LIMIT)))
        );
    }

    private List<LabeledCountResponse> toLabeledCounts(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new LabeledCountResponse((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    private List<AssetUsageResponse> toAssetUsage(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new AssetUsageResponse((UUID) row[0], (String) row[1], ((Number) row[2]).longValue()))
                .toList();
    }
}
