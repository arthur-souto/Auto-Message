package com.arthursouto.service;

import com.arthursouto.dto.PlanResponse;
import com.arthursouto.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> findAll() {
        return planRepository.findAll().stream()
                .map(PlanResponse::from)
                .toList();
    }
}
