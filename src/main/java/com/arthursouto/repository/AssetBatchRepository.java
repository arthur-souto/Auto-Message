package com.arthursouto.repository;

import com.arthursouto.domain.AssetBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssetBatchRepository extends JpaRepository<AssetBatch, UUID> {

    List<AssetBatch> findByAssetId(UUID assetId);
}
