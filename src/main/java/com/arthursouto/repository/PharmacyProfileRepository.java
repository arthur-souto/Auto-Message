package com.arthursouto.repository;

import com.arthursouto.domain.PharmacyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PharmacyProfileRepository extends JpaRepository<PharmacyProfile, UUID> {

    Optional<PharmacyProfile> findByUserId(UUID userId);
}
