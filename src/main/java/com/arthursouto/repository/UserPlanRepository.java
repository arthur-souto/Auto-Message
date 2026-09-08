package com.arthursouto.repository;

import com.arthursouto.domain.UserPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPlanRepository extends JpaRepository<UserPlan, UUID> {

    Optional<UserPlan> findByUserId(UUID userId);
}
