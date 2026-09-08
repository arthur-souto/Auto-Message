package com.arthursouto.service;

import com.arthursouto.domain.Plan;
import com.arthursouto.domain.User;
import com.arthursouto.domain.UserPlan;
import com.arthursouto.dto.UserPlanRequest;
import com.arthursouto.dto.UserPlanResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.PlanRepository;
import com.arthursouto.repository.UserPlanRepository;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.rules.PlanStatus;
import com.arthursouto.rules.PlanType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPlanServiceTest {

    @Mock
    private UserPlanRepository userPlanRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPlanService userPlanService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private User authenticateAsVerifiedUser() {
        User user = UserFactory.userBuilder().isVerified(true).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null)
        );
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        return user;
    }

    private Plan freePlan() {
        return Plan.builder()
                .id(UUID.randomUUID())
                .type(PlanType.FREE)
                .name("Gratuito")
                .maxFormulas(5)
                .maxAssets(20)
                .build();
    }

    @Test
    void findMineThrowsWhenNoActivePlan() {
        User user = authenticateAsVerifiedUser();
        when(userPlanRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPlanService.findMine())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignThrowsWhenPlanTypeUnknown() {
        authenticateAsVerifiedUser();
        when(planRepository.findByType(PlanType.PRO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPlanService.assign(new UserPlanRequest(PlanType.PRO, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignCreatesUserPlanWhenNoneExists() {
        User user = authenticateAsVerifiedUser();
        Plan plan = freePlan();
        when(planRepository.findByType(PlanType.FREE)).thenReturn(Optional.of(plan));
        when(userPlanRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(userPlanRepository.save(any(UserPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPlanResponse response = userPlanService.assign(new UserPlanRequest(PlanType.FREE, null));

        assertThat(response.plan().type()).isEqualTo(PlanType.FREE);
        assertThat(response.status()).isEqualTo(PlanStatus.ACTIVE);
        assertThat(response.startedAt()).isNotNull();
    }

    @Test
    void assignUpdatesExistingUserPlan() {
        User user = authenticateAsVerifiedUser();
        Plan proPlan = Plan.builder().id(UUID.randomUUID()).type(PlanType.PRO).name("Profissional").build();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        UserPlan existing = UserPlan.builder()
                .id(UUID.randomUUID())
                .user(user)
                .plan(freePlan())
                .status(PlanStatus.ACTIVE)
                .startedAt(Instant.now().minusSeconds(3600))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(planRepository.findByType(PlanType.PRO)).thenReturn(Optional.of(proPlan));
        when(userPlanRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));
        when(userPlanRepository.save(any(UserPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPlanResponse response = userPlanService.assign(new UserPlanRequest(PlanType.PRO, expiresAt));

        assertThat(response.plan().type()).isEqualTo(PlanType.PRO);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }
}
