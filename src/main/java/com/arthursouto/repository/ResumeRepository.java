package com.arthursouto.repository;

import com.arthursouto.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserId(UUID userId);

    Optional<Resume> findByIdAndUserId(Long id, UUID userid);

}
