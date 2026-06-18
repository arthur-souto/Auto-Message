package com.arthursouto.repository;

import com.arthursouto.domain.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    List<WorkExperience> findByResumeIdOrderByOrderIndexAsc(Long resumeId);
}
