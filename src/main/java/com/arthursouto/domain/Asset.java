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

    @Column(columnDefinition = "TEXT")
    private String associations;

    @Column(name = "pharma_forms", columnDefinition = "TEXT")
    private String pharmaForms;

    @Column(name = "literature_url", length = 500)
    private String literatureUrl;

    @Column(length = 100)
    private String category;

    @Column(name = "is_exclusive", nullable = false)
    @Builder.Default
    private boolean isExclusive = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private List<AssetIndication> indications;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
