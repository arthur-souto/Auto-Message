package com.arthursouto.controller;

import com.arthursouto.dto.AssetIndicationRequest;
import com.arthursouto.dto.AssetIndicationResponse;
import com.arthursouto.service.AssetIndicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/assets/{assetId}/indications")
@RequiredArgsConstructor
public class AssetIndicationController {

    private final AssetIndicationService assetIndicationService;

    @GetMapping
    public Page<AssetIndicationResponse> findAll(@PathVariable UUID assetId, Pageable pageable) {
        return assetIndicationService.findAllByAssetId(assetId, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetIndicationResponse addIndication(@PathVariable UUID assetId,
                                                  @Valid @RequestBody AssetIndicationRequest request) {
        return assetIndicationService.addIndication(assetId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{indicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIndication(@PathVariable UUID assetId, @PathVariable UUID indicationId) {
        assetIndicationService.deleteIndication(assetId, indicationId);
    }
}
