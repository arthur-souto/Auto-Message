package com.arthursouto.controller;

import com.arthursouto.dto.AssetIncompatibilityRequest;
import com.arthursouto.dto.AssetIncompatibilityResponse;
import com.arthursouto.service.AssetIncompatibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/assets/{assetId}/incompatibilities")
@RequiredArgsConstructor
public class AssetIncompatibilityController {

    private final AssetIncompatibilityService assetIncompatibilityService;

    @GetMapping
    public Page<AssetIncompatibilityResponse> findAll(@PathVariable UUID assetId, Pageable pageable) {
        return assetIncompatibilityService.findAllByAssetId(assetId, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetIncompatibilityResponse add(@PathVariable UUID assetId,
                                             @Valid @RequestBody AssetIncompatibilityRequest request) {
        return assetIncompatibilityService.addIncompatibility(assetId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{incompatibilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID assetId, @PathVariable UUID incompatibilityId) {
        assetIncompatibilityService.deleteIncompatibility(assetId, incompatibilityId);
    }
}
