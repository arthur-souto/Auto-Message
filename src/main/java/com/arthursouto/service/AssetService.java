package com.arthursouto.service;

import com.arthursouto.dto.AssetResponse;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.AssetBatchRepository;
import com.arthursouto.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public Page<AssetResponse> searchAssets(String target, Pageable pageable) {
        AuthenticatedUser.isAccountVerified();

        if(target == null) {
            return  assetRepository.findAll(pageable).map(AssetResponse::from);
        }

        return assetRepository.searchAssets(target.trim(), pageable).map(AssetResponse::from);
    }
}
