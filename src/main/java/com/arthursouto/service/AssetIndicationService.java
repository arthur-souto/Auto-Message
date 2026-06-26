package com.arthursouto.service;

import com.arthursouto.domain.AssetIndication;
import com.arthursouto.dto.AssetIndicationRequest;
import com.arthursouto.dto.AssetIndicationResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.AssetIndicationRepository;
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
public class AssetIndicationService {

    private final AssetIndicationRepository assetIndicationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AssetIndicationResponse> findAllByAssetId(UUID assetId, Pageable pageable) {
        AuthenticatedUser.isAccountVerified(userRepository);

        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found");
        }

        return assetIndicationRepository.findAllByAssetId(assetId, pageable).map(AssetIndicationResponse::from);
    }

    @Transactional
    public AssetIndicationResponse addIndication(UUID assetId, AssetIndicationRequest request) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        final var indication = AssetIndication.builder()
                .asset(asset)
                .indication(request.indication())
                .build();

        return AssetIndicationResponse.from(assetIndicationRepository.save(indication));
    }

    @Transactional
    public void deleteIndication(UUID assetId, UUID indicationId) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var indication = assetIndicationRepository.findByIdAndAssetId(indicationId, assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Indication not found"));

        assetIndicationRepository.delete(indication);
    }
}
