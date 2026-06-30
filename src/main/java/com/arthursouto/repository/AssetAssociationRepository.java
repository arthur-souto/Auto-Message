package com.arthursouto.repository;

import com.arthursouto.domain.AssetAssociation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetAssociationRepository extends JpaRepository<AssetAssociation, UUID> {

    Page<AssetAssociation> findAllByAssetId(UUID assetId, Pageable pageable);

    Optional<AssetAssociation> findByIdAndAssetId(UUID id, UUID assetId);
}
