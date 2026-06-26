package com.arthursouto.repository;

import com.arthursouto.domain.AssetIndication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetIndicationRepository extends JpaRepository<AssetIndication, UUID> {

    Page<AssetIndication> findAllByAssetId(UUID assetId, Pageable pageable);

    Optional<AssetIndication> findByIdAndAssetId(UUID id, UUID assetId);
}
