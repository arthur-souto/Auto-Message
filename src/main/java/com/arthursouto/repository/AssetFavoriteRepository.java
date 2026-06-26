package com.arthursouto.repository;

import com.arthursouto.domain.AssetFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

public interface AssetFavoriteRepository extends JpaRepository<AssetFavorite, UUID> {

    boolean existsByUserIdAndAssetId(UUID userId, UUID assetId);

    Page<AssetFavorite> findAllByUserId(UUID userId, Pageable pageable);

    @Modifying
    void deleteByUserIdAndAssetId(UUID userId, UUID assetId);
}
