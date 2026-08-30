package com.arthursouto.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "asset_incompatibilities", indexes = {
        @Index(name = "idx_asset_incompatibilities_a", columnList = "asset_a_id"),
        @Index(name = "idx_asset_incompatibilities_b", columnList = "asset_b_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetIncompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_a_id", nullable = false)
    private Asset assetA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_b_id", nullable = false)
    private Asset assetB;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
