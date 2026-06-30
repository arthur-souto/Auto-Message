package com.arthursouto.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_assets_name", columnList = "name"),
        @Index(name = "idx_assets_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    private String supplier;

    @Column(length = 20)
    private String unit;

    private String manufacturer;

    @Column(columnDefinition = "TEXT")
    private String composition;

    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String mechanism;

    @Column(name = "pharma_forms", columnDefinition = "TEXT")
    private String pharmaForms;

    @Column(name = "literature_url", length = 500)
    private String literatureUrl;

    @Column(length = 100)
    private String category;

    @Column(name = "is_exclusive", nullable = false)
    @Builder.Default
    private boolean isExclusive = false;

    @Column(name = "concentration_min", precision = 10, scale = 4)
    private java.math.BigDecimal concentrationMin;

    @Column(name = "concentration_max", precision = 10, scale = 4)
    private java.math.BigDecimal concentrationMax;

    @Column(name = "concentration_usual", precision = 10, scale = 4)
    private java.math.BigDecimal concentrationUsual;

    @Column(name = "concentration_unit", length = 20)
    private String concentrationUnit;

    @Column(name = "concentration_source", length = 100)
    private String concentrationSource;

    @Column(name = "concentration_pharma_form", length = 100)
    private String concentrationPharmaForm;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
