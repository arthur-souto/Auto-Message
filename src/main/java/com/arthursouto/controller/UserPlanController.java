package com.arthursouto.controller;

import com.arthursouto.dto.UserPlanRequest;
import com.arthursouto.dto.UserPlanResponse;
import com.arthursouto.service.UserPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/user-plans/me")
@RequiredArgsConstructor
public class UserPlanController {

    private final UserPlanService userPlanService;

    @GetMapping
    public UserPlanResponse findMine() {
        return userPlanService.findMine();
    }

    @PutMapping
    public UserPlanResponse assign(@Valid @RequestBody UserPlanRequest request) {
        return userPlanService.assign(request);
    }
}
