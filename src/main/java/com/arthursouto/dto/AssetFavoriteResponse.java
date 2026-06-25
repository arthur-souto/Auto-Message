package com.arthursouto.dto;

import com.arthursouto.domain.AssetFavorite;

import java.time.Instant;
import java.util.UUID;

public record AssetFavoriteResponse(
        UUID id,
        AssetResponse asset,
        Instant createdAt
) {
    public static AssetFavoriteResponse from(AssetFavorite favorite) {
        return new AssetFavoriteResponse(
                favorite.getId(),
                AssetResponse.from(favorite.getAsset()),
                favorite.getCreatedAt()
        );
    }
}
