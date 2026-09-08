package com.arthursouto.repository;

import com.arthursouto.domain.AssetIncompatibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetIncompatibilityRepository extends JpaRepository<AssetIncompatibility, UUID> {

    @Query("SELECT i FROM AssetIncompatibility i WHERE i.assetA.id = :assetId OR i.assetB.id = :assetId")
    Page<AssetIncompatibility> findAllByAssetId(@Param("assetId") UUID assetId, Pageable pageable);

    @Query("""
        SELECT i FROM AssetIncompatibility i
        WHERE (i.assetA.id = :a AND i.assetB.id = :b)
           OR (i.assetA.id = :b AND i.assetB.id = :a)
    """)
    Optional<AssetIncompatibility> findByAssetPair(@Param("a") UUID a, @Param("b") UUID b);

    @Query("SELECT i FROM AssetIncompatibility i WHERE i.id = :id AND (i.assetA.id = :assetId OR i.assetB.id = :assetId)")
    Optional<AssetIncompatibility> findByIdAndInvolvingAsset(@Param("id") UUID id, @Param("assetId") UUID assetId);

    @Query("SELECT i FROM AssetIncompatibility i WHERE i.assetA.id IN :assetIds AND i.assetB.id IN :assetIds")
    List<AssetIncompatibility> findAllWithinAssetIds(@Param("assetIds") Collection<UUID> assetIds);
}
