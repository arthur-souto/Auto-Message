package com.arthursouto.service;

import com.arthursouto.domain.AssetAssociation;
import com.arthursouto.dto.AssetAssociationRequest;
import com.arthursouto.dto.AssetAssociationResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.AssetAssociationRepository;
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
public class AssetAssociationService {

    private final AssetAssociationRepository assetAssociationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public Page<AssetAssociationResponse> findAllByAssetId(UUID assetId, Pageable pageable) {
        AuthenticatedUser.isAccountVerified(userRepository);

        if(!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found");
        }

        return assetAssociationRepository.findAllByAssetId(assetId, pageable).map(AssetAssociationResponse::from);
    }

    @Transactional
    public AssetAssociationResponse addAssociation(UUID assetId, AssetAssociationRequest req) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        final var indication = AssetAssociation.builder()
                .asset(asset)
                .association(req.association())
                .build();

        return AssetAssociationResponse.from(assetAssociationRepository.save(indication));
    }

    @Transactional
    public void deleteIndication(UUID assetId, UUID indicationId) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var indication = assetAssociationRepository.findByIdAndAssetId(indicationId, assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        assetAssociationRepository.delete(indication);
    }
}
