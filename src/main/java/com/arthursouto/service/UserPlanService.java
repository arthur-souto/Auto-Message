package com.arthursouto.service;

import com.arthursouto.domain.Plan;
import com.arthursouto.domain.User;
import com.arthursouto.domain.UserPlan;
import com.arthursouto.dto.UserPlanRequest;
import com.arthursouto.dto.UserPlanResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.PlanRepository;
import com.arthursouto.repository.UserPlanRepository;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.rules.PlanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserPlanService {

    private final UserPlanRepository userPlanRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserPlanResponse findMine() {
        User user = AuthenticatedUser.user(userRepository);
        UserPlan userPlan = userPlanRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Active plan not found"));

        return UserPlanResponse.from(userPlan);
    }

    @Transactional
    public UserPlanResponse assign(UserPlanRequest request) {
        User user = AuthenticatedUser.user(userRepository);
        return assignUser(request, user);
    }

    public UserPlanResponse assignUser(UserPlanRequest request, User user) {
        Plan plan = planRepository.findByType(request.planType())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        UserPlan userPlan = userPlanRepository.findByUserId(user.getId())
                .orElseGet(() -> UserPlan.builder().user(user).build());

        userPlan.setPlan(plan);
        userPlan.setStatus(PlanStatus.ACTIVE);
        userPlan.setStartedAt(Instant.now());
        userPlan.setExpiresAt(request.expiresAt());

        return UserPlanResponse.from(userPlanRepository.save(userPlan));
    }
}
