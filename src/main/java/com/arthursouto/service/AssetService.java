package com.arthursouto.service;

import com.arthursouto.dto.AssetResponse;
import com.arthursouto.dto.AssetUpdateRequest;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
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
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AssetResponse> searchAssets(String target, Pageable pageable) {
        AuthenticatedUser.isAccountVerified(userRepository);

        if (target == null) {
            return assetRepository.findAll(pageable).map(AssetResponse::from);
        }

        return assetRepository.searchAssets(target.trim(), pageable).map(AssetResponse::from);
    }

    @Transactional
    public AssetResponse updateAsset(UUID id, AssetUpdateRequest request) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        if (request.name() != null) asset.setName(request.name());
        if (request.supplier() != null) asset.setSupplier(request.supplier());
        if (request.unit() != null) asset.setUnit(request.unit());
        if (request.manufacturer() != null) asset.setManufacturer(request.manufacturer());
        if (request.composition() != null) asset.setComposition(request.composition());
        if (request.dosage() != null) asset.setDosage(request.dosage());
        if (request.mechanism() != null) asset.setMechanism(request.mechanism());
        if (request.associations() != null) asset.setAssociations(request.associations());
        if (request.pharmaForms() != null) asset.setPharmaForms(request.pharmaForms());
        if (request.literatureUrl() != null) asset.setLiteratureUrl(request.literatureUrl());
        if (request.category() != null) asset.setCategory(request.category());
        if (request.isExclusive() != null) asset.setExclusive(request.isExclusive());

        return AssetResponse.from(assetRepository.save(asset));
    }
}
