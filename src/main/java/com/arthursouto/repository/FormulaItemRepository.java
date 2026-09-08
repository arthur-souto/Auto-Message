package com.arthursouto.repository;

import com.arthursouto.domain.FormulaItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FormulaItemRepository extends JpaRepository<FormulaItem, UUID> {

    @Query("""
        SELECT
            CASE
                WHEN fi.concentration IS NULL THEN 'NOT_CHECKED'
                WHEN a.concentrationMin IS NULL AND a.concentrationMax IS NULL THEN 'NO_DATA'
                WHEN a.concentrationMin IS NOT NULL AND fi.concentration < a.concentrationMin THEN 'BELOW_MIN'
                WHEN a.concentrationMax IS NOT NULL AND fi.concentration > a.concentrationMax THEN 'ABOVE_MAX'
                ELSE 'WITHIN_RANGE'
            END,
            COUNT(fi)
        FROM FormulaItem fi
        JOIN fi.asset a
        JOIN fi.formula f
        WHERE f.user.id = :userId
        GROUP BY 1
    """)
    List<Object[]> countByConcentrationStatus(@Param("userId") UUID userId);

    @Query("""
        SELECT a.id, a.name, COUNT(fi)
        FROM FormulaItem fi
        JOIN fi.asset a
        JOIN fi.formula f
        WHERE f.user.id = :userId
        GROUP BY a.id, a.name
        ORDER BY COUNT(fi) DESC
    """)
    List<Object[]> topAssetsUsedInFormulas(@Param("userId") UUID userId, Pageable pageable);
}
