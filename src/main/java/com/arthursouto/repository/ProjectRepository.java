package com.arthursouto.repository;

import com.arthursouto.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByResumeIdOrderByOrderIndexAsc(Long resumeId);
}
