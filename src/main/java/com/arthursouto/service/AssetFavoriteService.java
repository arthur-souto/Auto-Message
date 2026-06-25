package com.arthursouto.service;

import com.arthursouto.domain.AssetFavorite;
import com.arthursouto.dto.AssetFavoriteResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.AssetFavoriteRepository;
import com.arthursouto.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetFavoriteService {

    private final AssetFavoriteRepository assetFavoriteRepository;
    private final AssetRepository assetRepository;


    @Transactional(readOnly = true)
    public Page<AssetFavoriteResponse> findAll(Pageable pageable) {
        final var user = AuthenticatedUser.isAccountVerifiedAndReturn();
        return assetFavoriteRepository.findAllByUserId(user.getId(), pageable).map(AssetFavoriteResponse::from);
    }


    @Transactional
    public void toggleAsset(UUID assetId) {
        final var user = AuthenticatedUser.isAccountVerifiedAndReturn();

        if(assetFavoriteRepository.existsByUserIdAndAssetId(user.getId(), assetId)) {
            assetFavoriteRepository.deleteByUserIdAndAssetId(user.getId(), assetId);
            return;
        }

        final var asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        final var favoriteAsset = AssetFavorite.builder()
                .user(user)
                .asset(asset)
                .build();

        assetFavoriteRepository.save(favoriteAsset);
    }

}
