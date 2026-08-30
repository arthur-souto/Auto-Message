package com.arthursouto.repository;

import com.arthursouto.domain.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Page<Doctor> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Doctor> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
