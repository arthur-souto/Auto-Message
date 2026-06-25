package com.arthursouto.controller;

import com.arthursouto.dto.AssetFavoriteResponse;
import com.arthursouto.service.AssetFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/assets/favorites")
@RequiredArgsConstructor
public class AssetFavoriteController {

    private final AssetFavoriteService assetFavoriteService;

    @GetMapping
    public Page<AssetFavoriteResponse> findAll(Pageable pageable) {
        return assetFavoriteService.findAll(pageable);
    }

    @PutMapping("/{assetId}/toggle")
    public void toggleAsset(@PathVariable UUID assetId) {
        assetFavoriteService.toggleAsset(assetId);
    }
}
