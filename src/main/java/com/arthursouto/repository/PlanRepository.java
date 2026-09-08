package com.arthursouto.repository;

import com.arthursouto.domain.Plan;
import com.arthursouto.rules.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Optional<Plan> findByType(PlanType type);
}
