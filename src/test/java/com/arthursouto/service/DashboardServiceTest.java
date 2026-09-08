package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.DashboardSummaryResponse;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.AssetFavoriteRepository;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.DoctorRepository;
import com.arthursouto.repository.FormulaItemRepository;
import com.arthursouto.repository.FormulaRepository;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private FormulaRepository formulaRepository;

    @Mock
    private FormulaItemRepository formulaItemRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AssetFavoriteRepository assetFavoriteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private User authenticateAsVerifiedUser() {
        User user = UserFactory.userBuilder().isVerified(true).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null)
        );
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void aggregatesAllCountersIntoSummary() {
        User user = authenticateAsVerifiedUser();

        when(assetRepository.count()).thenReturn(75L);
        when(formulaRepository.countByUserId(user.getId())).thenReturn(41L);
        when(doctorRepository.countByUserId(user.getId())).thenReturn(8L);
        when(assetFavoriteRepository.countByUserId(user.getId())).thenReturn(3L);
        when(formulaRepository.countFormulasWithIncompatibilities(user.getId())).thenReturn(12L);

        when(assetRepository.countByCategory()).thenReturn(List.of(
                new Object[]{"Vitaminas", 20L},
                new Object[]{"Sem categoria", 5L}
        ));
        when(formulaItemRepository.countByConcentrationStatus(user.getId())).thenReturn(List.of(
                new Object[]{"WITHIN_RANGE", 40L},
                new Object[]{"ABOVE_MAX", 15L}
        ));
        when(formulaRepository.countByCreationMonth(user.getId())).thenReturn(List.of(
                new Object[]{"2026-07", 10L},
                new Object[]{"2026-08", 31L}
        ));
        UUID assetId = UUID.randomUUID();
        when(formulaItemRepository.topAssetsUsedInFormulas(any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{assetId, "Melatonina", 9L}
        ));

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.totalAssets()).isEqualTo(75L);
        assertThat(summary.totalFormulas()).isEqualTo(41L);
        assertThat(summary.totalDoctors()).isEqualTo(8L);
        assertThat(summary.totalFavorites()).isEqualTo(3L);
        assertThat(summary.formulasWithIncompatibilityWarnings()).isEqualTo(12L);

        assertThat(summary.assetsByCategory()).hasSize(2);
        assertThat(summary.assetsByCategory().getFirst().label()).isEqualTo("Vitaminas");
        assertThat(summary.assetsByCategory().getFirst().count()).isEqualTo(20L);

        assertThat(summary.concentrationStatusBreakdown()).hasSize(2);
        assertThat(summary.formulasCreatedByMonth()).hasSize(2);

        assertThat(summary.topAssetsUsedInFormulas()).hasSize(1);
        assertThat(summary.topAssetsUsedInFormulas().getFirst().assetId()).isEqualTo(assetId);
        assertThat(summary.topAssetsUsedInFormulas().getFirst().assetName()).isEqualTo("Melatonina");
        assertThat(summary.topAssetsUsedInFormulas().getFirst().usageCount()).isEqualTo(9L);
    }
}
