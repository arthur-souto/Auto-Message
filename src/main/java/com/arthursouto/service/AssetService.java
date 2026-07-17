package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.dto.AssetResponse;
import com.arthursouto.dto.AssetUpdateRequest;
import com.arthursouto.dto.ConcentrationCheckResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.mapper.AssetMapper;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.rules.ConcentrationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetMapper assetMapper;

    @Transactional(readOnly = true)
    public Page<AssetResponse> searchAssets(String target, Pageable pageable) {
        AuthenticatedUser.isAccountVerified(userRepository);

        if (target == null) {
            return assetRepository.findAll(pageable).map(AssetResponse::from);
        }

        return assetRepository.searchAssets(target.trim(), pageable).map(AssetResponse::from);
    }

    @Transactional
    public AssetResponse updateAsset(UUID id, AssetUpdateRequest request) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        assetMapper.updateAsset(request, asset);

        return AssetResponse.from(assetRepository.save(asset));
    }

    @Transactional
    public void deleteAsset(UUID id) {
        AuthenticatedUser.isAccountVerified(userRepository);

        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asset not found");
        }

        assetRepository.deleteById(id);
    }

    @Transactional
    public void deleteAssets(List<UUID> ids) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var foundIds = assetRepository.findAllById(ids).stream()
                .map(Asset::getId)
                .collect(Collectors.toSet());

        final var missingIds = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Assets not found: " + missingIds);
        }

        assetRepository.deleteAllById(ids);
    }

    @Transactional(readOnly = true)
    public ConcentrationCheckResponse checkConcentration(UUID id, BigDecimal value) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        final var min = asset.getConcentrationMin();
        final var max = asset.getConcentrationMax();

        ConcentrationStatus status;
        if (min == null && max == null) {
            status = ConcentrationStatus.NO_DATA;
        } else if (min != null && value.compareTo(min) < 0) {
            status = ConcentrationStatus.BELOW_MIN;
        } else if (max != null && value.compareTo(max) > 0) {
            status = ConcentrationStatus.ABOVE_MAX;
        } else {
            status = ConcentrationStatus.WITHIN_RANGE;
        }

        return new ConcentrationCheckResponse(
                value,
                status,
                min,
                max,
                asset.getConcentrationUsual(),
                asset.getConcentrationUnit(),
                asset.getConcentrationSource(),
                asset.getConcentrationPharmaForm()
        );
    }
}
