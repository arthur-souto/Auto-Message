package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.dto.AssetResponse;
import com.arthursouto.dto.AssetUpdateRequest;
import com.arthursouto.dto.ConcentrationCheckResponse;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.mapper.AssetMapper;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.rules.ConcentrationChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        try {
            assetRepository.deleteById(id);
            assetRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Asset is used in an existing formula and cannot be deleted");
        }
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

        try {
            assetRepository.deleteAllById(ids);
            assetRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Some assets are used in existing formulas and cannot be deleted");
        }
    }

    @Transactional(readOnly = true)
    public ConcentrationCheckResponse checkConcentration(UUID id, BigDecimal value) {
        AuthenticatedUser.isAccountVerified(userRepository);

        final var asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        final var min = asset.getConcentrationMin();
        final var max = asset.getConcentrationMax();

        final var status = ConcentrationChecker.classify(min, max, value);

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
