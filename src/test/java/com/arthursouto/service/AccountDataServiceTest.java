package com.arthursouto.service;

import com.arthursouto.domain.PharmacyProfile;
import com.arthursouto.domain.User;
import com.arthursouto.dto.AccountDataExportResponse;
import com.arthursouto.dto.FormulaResponse;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.AssetFavoriteRepository;
import com.arthursouto.repository.DoctorRepository;
import com.arthursouto.repository.PatientRepository;
import com.arthursouto.repository.PharmacyProfileRepository;
import com.arthursouto.repository.UserPlanRepository;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountDataServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PharmacyProfileRepository pharmacyProfileRepository;
    @Mock
    private UserPlanRepository userPlanRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private FormulaService formulaService;
    @Mock
    private AssetFavoriteRepository assetFavoriteRepository;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AccountDataService accountDataService;

    @Test
    void exportMyDataAssemblesEverySectionWithNullsWhenAbsent() {
        User user = UserFactory.user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(pharmacyProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(userPlanRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(patientRepository.findAllByUserId(any(), any(Pageable.class))).thenReturn(Page.empty());
        when(doctorRepository.findAllByUserId(any(), any(Pageable.class))).thenReturn(Page.empty());
        when(formulaService.findAllForExport(user.getId())).thenReturn(List.of());
        when(assetFavoriteRepository.findAllByUserId(any(), any(Pageable.class))).thenReturn(Page.empty());

        AccountDataExportResponse export = accountDataService.exportMyData(user.getId());

        assertThat(export.account().id()).isEqualTo(user.getId());
        assertThat(export.pharmacyProfile()).isNull();
        assertThat(export.plan()).isNull();
        assertThat(export.patients()).isEmpty();
        assertThat(export.doctors()).isEmpty();
        assertThat(export.formulas()).isEmpty();
        assertThat(export.favoriteAssets()).isEmpty();
        assertThat(export.exportedAt()).isNotNull();
    }

    @Test
    void exportMyDataIncludesFormulasFromFormulaService() {
        User user = UserFactory.user();
        FormulaResponse formula = new FormulaResponse(
                UUID.randomUUID(), "Fórmula X", null, null, null, null, null, List.of(), List.of(), Instant.now(), Instant.now()
        );
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(pharmacyProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(userPlanRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(patientRepository.findAllByUserId(any(), any(Pageable.class))).thenReturn(Page.empty());
        when(doctorRepository.findAllByUserId(any(), any(Pageable.class))).thenReturn(Page.empty());
        when(formulaService.findAllForExport(user.getId())).thenReturn(List.of(formula));
        when(assetFavoriteRepository.findAllByUserId(any(), any(Pageable.class))).thenReturn(Page.empty());

        AccountDataExportResponse export = accountDataService.exportMyData(user.getId());

        assertThat(export.formulas()).containsExactly(formula);
    }

    @Test
    void deleteMyAccountScrubsPiiAndRemovesRelatedRecords() {
        User user = UserFactory.userBuilder().password("hashed").build();
        UUID originalId = user.getId();
        PharmacyProfile pharmacyProfile = PharmacyProfile.builder().id(UUID.randomUUID()).user(user).build();
        when(userRepository.findById(originalId)).thenReturn(Optional.of(user));
        when(pharmacyProfileRepository.findByUserId(originalId)).thenReturn(Optional.of(pharmacyProfile));
        when(userPlanRepository.findByUserId(originalId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountDataService.deleteMyAccount(originalId);

        verify(refreshTokenService).revokeAll(originalId);
        verify(pharmacyProfileRepository).delete(pharmacyProfile);
        verify(userPlanRepository, never()).delete(any());
        verify(assetFavoriteRepository).deleteAllByUserId(originalId);

        assertThat(user.getName()).isEqualTo("Usuário removido");
        assertThat(user.getEmail()).contains("@removed.invalid");
        assertThat(user.getUsername()).isEqualTo("deleted-" + originalId);
        assertThat(user.getPassword()).isNull();
        assertThat(user.getProfileImage()).isNull();
        assertThat(user.getGoogleId()).isNull();
        assertThat(user.isVerified()).isFalse();
        assertThat(user.getDeletedAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void deleteMyAccountIsNoOpWhenAlreadyDeleted() {
        User user = UserFactory.userBuilder().deletedAt(Instant.now()).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        accountDataService.deleteMyAccount(user.getId());

        verify(refreshTokenService, never()).revokeAll(any());
        verify(userRepository, never()).save(any());
    }
}
