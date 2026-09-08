package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.AccountDataExportResponse;
import com.arthursouto.dto.AssetFavoriteResponse;
import com.arthursouto.dto.DoctorResponse;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.dto.PatientResponse;
import com.arthursouto.dto.PharmacyProfileResponse;
import com.arthursouto.dto.UserPlanResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.repository.AssetFavoriteRepository;
import com.arthursouto.repository.DoctorRepository;
import com.arthursouto.repository.PatientRepository;
import com.arthursouto.repository.PharmacyProfileRepository;
import com.arthursouto.repository.UserPlanRepository;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountDataService {

    private final UserRepository userRepository;
    private final PharmacyProfileRepository pharmacyProfileRepository;
    private final UserPlanRepository userPlanRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final FormulaService formulaService;
    private final AssetFavoriteRepository assetFavoriteRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public AccountDataExportResponse exportMyData(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PharmacyProfileResponse pharmacyProfile = pharmacyProfileRepository.findByUserId(userId)
                .map(PharmacyProfileResponse::from).orElse(null);
        UserPlanResponse plan = userPlanRepository.findByUserId(userId)
                .map(UserPlanResponse::from).orElse(null);
        var patients = patientRepository.findAllByUserId(userId, Pageable.unpaged())
                .map(PatientResponse::from).getContent();
        var doctors = doctorRepository.findAllByUserId(userId, Pageable.unpaged())
                .map(DoctorResponse::from).getContent();
        var formulas = formulaService.findAllForExport(userId);
        var favorites = assetFavoriteRepository.findAllByUserId(userId, Pageable.unpaged())
                .map(AssetFavoriteResponse::from).getContent();

        return new AccountDataExportResponse(
                MeResponse.from(user),
                pharmacyProfile,
                plan,
                patients,
                doctors,
                formulas,
                favorites,
                Instant.now()
        );
    }

    @Transactional
    public void deleteMyAccount(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getDeletedAt() != null) {
            return;
        }

        refreshTokenService.revokeAll(userId);
        pharmacyProfileRepository.findByUserId(userId).ifPresent(pharmacyProfileRepository::delete);
        userPlanRepository.findByUserId(userId).ifPresent(userPlanRepository::delete);
        assetFavoriteRepository.deleteAllByUserId(userId);

        user.setName("Usuário removido");
        user.setEmail("deleted-" + UUID.randomUUID() + "@removed.invalid");
        user.setUsername("deleted-" + user.getId());
        user.setPassword(null);
        user.setProfileImage(null);
        user.setGoogleId(null);
        user.setVerified(false);
        user.setDeletedAt(Instant.now());

        userRepository.save(user);
    }
}
