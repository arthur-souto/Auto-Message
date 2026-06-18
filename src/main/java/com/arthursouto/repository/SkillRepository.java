package com.arthursouto.repository;

import com.arthursouto.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByResumeIdOrderByOrderIndexAsc(Long resumeId);
}
