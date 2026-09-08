package com.arthursouto.service;

import com.arthursouto.domain.Plan;
import com.arthursouto.dto.PlanResponse;
import com.arthursouto.repository.PlanRepository;
import com.arthursouto.rules.PlanType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    void findAllReturnsAllCatalogPlans() {
        Plan free = Plan.builder().id(UUID.randomUUID()).type(PlanType.FREE).name("Gratuito").maxFormulas(5).maxAssets(20).build();
        Plan pro = Plan.builder().id(UUID.randomUUID()).type(PlanType.PRO).name("Profissional").maxFormulas(100).maxAssets(500).build();
        when(planRepository.findAll()).thenReturn(List.of(free, pro));

        List<PlanResponse> response = planService.findAll();

        assertThat(response).hasSize(2);
        assertThat(response).extracting(PlanResponse::type).containsExactly(PlanType.FREE, PlanType.PRO);
    }
}
