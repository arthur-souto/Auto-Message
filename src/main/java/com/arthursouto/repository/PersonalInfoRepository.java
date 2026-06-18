package com.arthursouto.repository;

import com.arthursouto.domain.PersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, Long> {
    Optional<PersonalInfo> findByResumeId(Long resumeId);
}
