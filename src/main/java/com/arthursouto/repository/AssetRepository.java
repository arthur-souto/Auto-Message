package com.arthursouto.repository;

import com.arthursouto.domain.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
    SELECT a FROM Asset a
    WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :target, '%'))
    OR LOWER(a.supplier) LIKE LOWER(CONCAT('%', :target, '%'))
    """)
    Page<Asset> searchAssets(@Param("target") String target, Pageable pageable);
}
