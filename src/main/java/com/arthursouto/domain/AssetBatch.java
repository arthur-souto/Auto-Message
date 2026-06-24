package com.arthursouto.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "asset_batches", indexes = {
        @Index(name = "idx_asset_batches_asset_id", columnList = "asset_id"),
        @Index(name = "idx_asset_batches_expires_at", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(length = 100)
    private String batch;

    private LocalDate expiresAt;

    @Column(precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 15, scale = 8)
    private BigDecimal unitCost;

    @Column(precision = 15, scale = 4)
    private BigDecimal total;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
