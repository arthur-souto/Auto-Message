package com.arthursouto.service;

import com.arthursouto.domain.PharmacyProfile;
import com.arthursouto.domain.User;
import com.arthursouto.dto.PharmacyProfileRequest;
import com.arthursouto.dto.PharmacyProfileResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.PharmacyProfileRepository;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PharmacyProfileService {

    private final PharmacyProfileRepository pharmacyProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PharmacyProfileResponse findMine() {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        PharmacyProfile profile = pharmacyProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy profile not found"));

        return PharmacyProfileResponse.from(profile);
    }

    @Transactional
    public PharmacyProfileResponse save(PharmacyProfileRequest request) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        PharmacyProfile profile = pharmacyProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> PharmacyProfile.builder().user(user).build());

        profile.setPharmacyName(request.pharmacyName());
        profile.setAddress(request.address());
        profile.setPhone(request.phone());
        profile.setEmail(request.email());
        profile.setResponsibleName(request.responsibleName());
        profile.setResponsibleDocument(request.responsibleDocument());
        profile.setResponsibleRegistration(request.responsibleRegistration());

        return PharmacyProfileResponse.from(pharmacyProfileRepository.save(profile));
    }
}
