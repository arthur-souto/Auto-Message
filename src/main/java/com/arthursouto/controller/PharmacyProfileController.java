package com.arthursouto.controller;

import com.arthursouto.dto.PharmacyProfileRequest;
import com.arthursouto.dto.PharmacyProfileResponse;
import com.arthursouto.service.PharmacyProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/pharmacy-profile")
@RequiredArgsConstructor
public class PharmacyProfileController {

    private final PharmacyProfileService pharmacyProfileService;

    @GetMapping
    public PharmacyProfileResponse findMine() {
        return pharmacyProfileService.findMine();
    }

    @PutMapping
    public PharmacyProfileResponse save(@Valid @RequestBody PharmacyProfileRequest request) {
        return pharmacyProfileService.save(request);
    }
}
