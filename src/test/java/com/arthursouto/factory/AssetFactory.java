package com.arthursouto.factory;

import com.arthursouto.domain.Asset;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class AssetFactory {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private AssetFactory() {
    }

    public static Asset.AssetBuilder assetBuilder() {
        int n = SEQUENCE.incrementAndGet();
        Instant now = Instant.now();
        return Asset.builder()
                .id(UUID.randomUUID())
                .code("CODE-" + n)
                .name("Asset " + n)
                .supplier("Supplier " + n)
                .createdAt(now)
                .updatedAt(now);
    }

    public static Asset asset() {
        return assetBuilder().build();
    }
}
