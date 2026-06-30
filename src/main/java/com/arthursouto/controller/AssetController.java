package com.arthursouto.controller;

import com.arthursouto.dto.AssetResponse;
import com.arthursouto.dto.AssetUpdateRequest;
import com.arthursouto.dto.ConcentrationCheckResponse;
import com.arthursouto.service.AssetService;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/search")
    public Page<AssetResponse> searchAssets(@RequestParam(required = false) String target, Pageable pageable) {
        return assetService.searchAssets(target, pageable);
    }

    @GetMapping("/{id}/concentration-check")
    public ConcentrationCheckResponse checkConcentration(
            @PathVariable UUID id,
            @RequestParam @DecimalMin("0.0") BigDecimal value) {
        return assetService.checkConcentration(id, value);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public AssetResponse updateAsset(@PathVariable UUID id, @RequestBody AssetUpdateRequest request) {
        return assetService.updateAsset(id, request);
    }
}
