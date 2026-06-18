package com.arthursouto.repository;

import com.arthursouto.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByResumeIdOrderByOrderIndexAsc(Long resumeId);
}
