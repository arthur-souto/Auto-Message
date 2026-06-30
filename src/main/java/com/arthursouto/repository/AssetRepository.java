package com.arthursouto.repository;

import com.arthursouto.domain.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    @Query("""
    SELECT a FROM Asset a
    WHERE :target IS NULL
       OR unaccent(LOWER(a.name))         LIKE unaccent(LOWER(CONCAT('%', :target, '%')))
       OR unaccent(LOWER(a.supplier))     LIKE unaccent(LOWER(CONCAT('%', :target, '%')))
       OR unaccent(LOWER(a.manufacturer)) LIKE unaccent(LOWER(CONCAT('%', :target, '%')))
       OR unaccent(LOWER(a.category))     LIKE unaccent(LOWER(CONCAT('%', :target, '%')))
       OR EXISTS (
           SELECT 1 FROM AssetIndication i
           WHERE i.asset = a
           AND unaccent(LOWER(i.indication)) LIKE unaccent(LOWER(CONCAT('%', :target, '%')))
       )
       OR EXISTS (
           SELECT 1 FROM AssetAssociation i
            WHERE i.asset = a
            AND unaccent(LOWER(i.association)) LIKE unaccent(LOWER(CONCAT('%', :target, '%')))    
           )
    """)
    Page<Asset> searchAssets(@Param("target") String target, Pageable pageable);
}
