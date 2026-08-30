package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.domain.AssetIncompatibility;
import com.arthursouto.dto.AssetIncompatibilityRequest;
import com.arthursouto.dto.AssetIncompatibilityResponse;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.AssetIncompatibilityRepository;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class
AssetIncompatibilityService {

    private final AssetIncompatibilityRepository assetIncompatibilityRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AssetIncompatibilityResponse> findAllByAssetId(UUID assetId, Pageable pageable) {
        AuthenticatedUser.isAccountVerified(userRepository);

        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found");
        }

        return assetIncompatibilityRepository.findAllByAssetId(assetId, pageable)
                .map(incompatibility -> toResponse(incompatibility, assetId));
    }

    @Transactional
    public AssetIncompatibilityResponse addIncompatibility(UUID assetId, AssetIncompatibilityRequest request) {
        AuthenticatedUser.isAccountVerified(userRepository);

        if (assetId.equals(request.otherAssetId())) {
            throw new BadRequestException("An asset cannot be incompatible with itself");
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        Asset other = assetRepository.findById(request.otherAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Other asset not found"));

        if (assetIncompatibilityRepository.findByAssetPair(assetId, request.otherAssetId()).isPresent()) {
            throw new ConflictException("Incompatibility between these assets already exists");
        }

        boolean assetIsA = assetId.toString().compareTo(request.otherAssetId().toString()) < 0;

        AssetIncompatibility incompatibility = AssetIncompatibility.builder()
                .assetA(assetIsA ? asset : other)
                .assetB(assetIsA ? other : asset)
                .reason(request.reason())
                .build();

        return toResponse(assetIncompatibilityRepository.save(incompatibility), assetId);
    }

    @Transactional
    public void deleteIncompatibility(UUID assetId, UUID incompatibilityId) {
        AuthenticatedUser.isAccountVerified(userRepository);

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
