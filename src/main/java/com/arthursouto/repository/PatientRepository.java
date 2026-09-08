package com.arthursouto.repository;

import com.arthursouto.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Page<Patient> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Patient> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
