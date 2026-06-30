package com.arthursouto.controller;

import com.arthursouto.dto.AssetAssociationRequest;
import com.arthursouto.dto.AssetAssociationResponse;
import com.arthursouto.service.AssetAssociationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/assets/{assetId}/associations")
@RequiredArgsConstructor
public class AssetAssociationController {

    private final AssetAssociationService assetAssociationService;

    @GetMapping
    public Page<AssetAssociationResponse> findAll(@PathVariable UUID assetId, Pageable pageable) {
        return assetAssociationService.findAllByAssetId(assetId, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetAssociationResponse addIndication(@PathVariable UUID assetId,
                                                 @Valid @RequestBody AssetAssociationRequest request) {
        return assetAssociationService.addAssociation(assetId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{associationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIndication(@PathVariable UUID assetId, @PathVariable UUID associationId) {
        assetAssociationService.deleteIndication(assetId, associationId);
    }
}
