package com.arthursouto.repository;

import com.arthursouto.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByResumeIdOrderByOrderIndexAsc(Long resumeId);
}
