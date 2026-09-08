package com.arthursouto.repository;

import com.arthursouto.domain.Formula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormulaRepository extends JpaRepository<Formula, UUID> {

    Page<Formula> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Formula> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);

    @Query("""
        SELECT COUNT(DISTINCT f.id) FROM Formula f
        WHERE f.user.id = :userId
        AND EXISTS (
            SELECT 1 FROM AssetIncompatibility inc
            WHERE EXISTS (SELECT 1 FROM FormulaItem i1 WHERE i1.formula = f AND i1.asset = inc.assetA)
            AND EXISTS (SELECT 1 FROM FormulaItem i2 WHERE i2.formula = f AND i2.asset = inc.assetB)
        )
    """)
    long countFormulasWithIncompatibilities(@Param("userId") UUID userId);

    @Query(value = """
        SELECT to_char(created_at, 'YYYY-MM') AS ym, COUNT(*)
        FROM formulas
        WHERE user_id = :userId
        GROUP BY ym
        ORDER BY ym
    """, nativeQuery = true)
    List<Object[]> countByCreationMonth(@Param("userId") UUID userId);
}
