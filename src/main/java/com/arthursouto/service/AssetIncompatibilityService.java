package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.domain.AssetIncompatibility;
import com.arthursouto.dto.AssetIncompatibilityRequest;
import com.arthursouto.dto.AssetIncompatibilityResponse;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.repository.AssetIncompatibilityRepository;
import com.arthursouto.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class
AssetIncompatibilityService {

    private final AssetIncompatibilityRepository assetIncompatibilityRepository;
    private final AssetRepository assetRepository;

    private static final int INCOMPATIBILITY_COMPARE_THRESHOLD = 0;

    @Transactional(readOnly = true)
    public Page<AssetIncompatibilityResponse> findAllByAssetId(UUID assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found");
        }

        return assetIncompatibilityRepository.findAllByAssetId(assetId, pageable)
                .map(incompatibility -> toResponse(incompatibility, assetId));
    }

    @Transactional
    public AssetIncompatibilityResponse addIncompatibility(UUID assetId, AssetIncompatibilityRequest request) {
        if (assetId.equals(request.otherAssetId())) {
            log.info("Attempted to create an incompatibility between asset {} and itself", assetId);
            throw new BadRequestException("An asset cannot be incompatible with itself");
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        Asset other = assetRepository.findById(request.otherAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Other asset not found"));

        if (assetIncompatibilityRepository.findByAssetPair(assetId, request.otherAssetId()).isPresent()) {
            log.info("Attempted to create an incompatibility between asset {} and asset {} that already exists", assetId, request.otherAssetId());
            throw new ConflictException("Incompatibility between these assets already exists");
        }

        boolean assetIsA = assetId.toString().compareTo(request.otherAssetId().toString()) < INCOMPATIBILITY_COMPARE_THRESHOLD;

        log.info("Asset {} is {} in the incompatibility pair with asset {}", assetId, assetIsA ? "A" : "B", request.otherAssetId());

        AssetIncompatibility incompatibility = AssetIncompatibility.builder()
                .assetA(assetIsA ? asset : other)
                .assetB(assetIsA ? other : asset)
                .reason(request.reason())
                .build();

        return toResponse(assetIncompatibilityRepository.save(incompatibility), assetId);
    }

    @Transactional
    public void deleteIncompatibility(UUID assetId, UUID incompatibilityId) {
        AssetIncompatibility incompatibility = assetIncompatibilityRepository
                .findByIdAndInvolvingAsset(incompatibilityId, assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Incompatibility not found"));

        assetIncompatibilityRepository.delete(incompatibility);
    }

    private AssetIncompatibilityResponse toResponse(AssetIncompatibility incompatibility, UUID assetId) {
        Asset other = incompatibility.getAssetA().getId().equals(assetId)
                ? incompatibility.getAssetB()
                : incompatibility.getAssetA();

        return new AssetIncompatibilityResponse(
                incompatibility.getId(),
                other.getId(),
                other.getName(),
                incompatibility.getReason(),
                incompatibility.getCreatedAt()
        );
    }
}
