package com.arthursouto.service;

import com.arthursouto.domain.PharmacyProfile;
import com.arthursouto.domain.User;
import com.arthursouto.dto.PharmacyProfileRequest;
import com.arthursouto.dto.PharmacyProfileResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.PharmacyProfileRepository;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PharmacyProfileServiceTest {

    @Mock
    private PharmacyProfileRepository pharmacyProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PharmacyProfileService pharmacyProfileService;

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
    void findMineThrowsWhenProfileNotCreatedYet() {
        User user = authenticateAsVerifiedUser();
        when(pharmacyProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pharmacyProfileService.findMine())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveCreatesProfileWhenNoneExists() {
        User user = authenticateAsVerifiedUser();
        when(pharmacyProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(pharmacyProfileRepository.save(any(PharmacyProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PharmacyProfileResponse response = pharmacyProfileService.save(
                new PharmacyProfileRequest("Kaline Waismam", "Rua X, 123", "1199999999", "kaline@email.com", "Kaline Waismam", "037.261.499-02", "CRF 115070/SP")
        );

        assertThat(response.pharmacyName()).isEqualTo("Kaline Waismam");
        assertThat(response.responsibleDocument()).isEqualTo("037.261.499-02");
    }

    @Test
    void saveUpdatesExistingProfile() {
        User user = authenticateAsVerifiedUser();
        PharmacyProfile existing = PharmacyProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .pharmacyName("Old Name")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(pharmacyProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));
        when(pharmacyProfileRepository.save(any(PharmacyProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PharmacyProfileResponse response = pharmacyProfileService.save(
                new PharmacyProfileRequest("New Name", null, null, null, null, null, null)
        );

        assertThat(response.pharmacyName()).isEqualTo("New Name");
    }
}
