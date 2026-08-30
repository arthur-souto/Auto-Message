package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.domain.AssetIncompatibility;
import com.arthursouto.domain.Formula;
import com.arthursouto.domain.FormulaItem;
import com.arthursouto.domain.User;
import com.arthursouto.dto.FormulaItemRequest;
import com.arthursouto.dto.FormulaRequest;
import com.arthursouto.dto.FormulaResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.AssetFactory;
import com.arthursouto.factory.FormulaFactory;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.AssetIncompatibilityRepository;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.DoctorRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormulaServiceTest {

    @Mock
    private FormulaRepository formulaRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetIncompatibilityRepository assetIncompatibilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private FormulaService formulaService;

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
    void createSavesFormulaWithResolvedItems() {
        User user = authenticateAsVerifiedUser();
        Asset asset = AssetFactory.asset();
        when(assetRepository.findAllById(List.of(asset.getId()))).thenReturn(List.of(asset));
        when(formulaRepository.save(any(Formula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FormulaItemRequest itemRequest = new FormulaItemRequest(asset.getId(), BigDecimal.TEN, "mg", BigDecimal.valueOf(5));
        FormulaRequest request = new FormulaRequest("Fórmula emagrecimento", "desc", null, List.of(itemRequest));

        FormulaResponse response = formulaService.create(request);

        assertThat(response.name()).isEqualTo("Fórmula emagrecimento");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().assetId()).isEqualTo(asset.getId());
        assertThat(response.incompatibilities()).isEmpty();
    }

    @Test
    void createThrowsWhenAssetDoesNotExist() {
        authenticateAsVerifiedUser();
        UUID missingAssetId = UUID.randomUUID();
        when(assetRepository.findAllById(List.of(missingAssetId))).thenReturn(List.of());

        FormulaItemRequest itemRequest = new FormulaItemRequest(missingAssetId, BigDecimal.TEN, "mg", null);
        FormulaRequest request = new FormulaRequest("Fórmula", null, null, List.of(itemRequest));

        assertThatThrownBy(() -> formulaService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(missingAssetId.toString());

        verify(formulaRepository, never()).save(any());
    }

    @Test
    void findByIdThrowsWhenFormulaDoesNotBelongToUser() {
        User user = authenticateAsVerifiedUser();
        UUID formulaId = UUID.randomUUID();
        when(formulaRepository.findByIdAndUserId(formulaId, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> formulaService.findById(formulaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Formula not found");
    }

    @Test
    void findByIdReturnsIncompatibilityWarningsBetweenFormulaAssets() {
        User user = authenticateAsVerifiedUser();
        Asset assetA = AssetFactory.asset();
        Asset assetB = AssetFactory.asset();

        Formula formula = FormulaFactory.formula(user);
        formula.getItems().add(buildItem(formula, assetA));
        formula.getItems().add(buildItem(formula, assetB));

        when(formulaRepository.findByIdAndUserId(formula.getId(), user.getId())).thenReturn(Optional.of(formula));

        AssetIncompatibility incompatibility = AssetIncompatibility.builder()
                .id(UUID.randomUUID())
                .assetA(assetA)
                .assetB(assetB)
                .reason("Reduz a absorção do outro ativo")
                .createdAt(Instant.now())
                .build();
        when(assetIncompatibilityRepository.findAllWithinAssetIds(any())).thenReturn(List.of(incompatibility));

        FormulaResponse response = formulaService.findById(formula.getId());

        assertThat(response.incompatibilities()).hasSize(1);
        assertThat(response.incompatibilities().getFirst().reason()).isEqualTo("Reduz a absorção do outro ativo");
    }

    @Test
    void deleteRemovesFormulaOwnedByUser() {
        User user = authenticateAsVerifiedUser();
        Formula formula = FormulaFactory.formula(user);
        when(formulaRepository.findByIdAndUserId(formula.getId(), user.getId())).thenReturn(Optional.of(formula));

        formulaService.delete(formula.getId());

        verify(formulaRepository).delete(formula);
    }

    private FormulaItem buildItem(Formula formula, Asset asset) {
        return FormulaItem.builder()
                .id(UUID.randomUUID())
                .formula(formula)
                .asset(asset)
                .quantity(BigDecimal.TEN)
                .unit("mg")
                .concentration(BigDecimal.valueOf(5))
                .createdAt(Instant.now())
                .build();
    }
}
