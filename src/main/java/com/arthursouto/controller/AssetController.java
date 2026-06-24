package com.arthursouto.controller;

import com.arthursouto.dto.AssetResponse;
import com.arthursouto.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/search")
    public Page<AssetResponse> searchAssets(@RequestParam(required = false) String target, Pageable pageable) {
        return assetService.searchAssets(target, pageable);
    }
}
