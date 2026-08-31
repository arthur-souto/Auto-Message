package com.arthursouto.service;

import com.arthursouto.dto.AssetRequest;
import com.arthursouto.exception.AssetImportRowException;
import com.arthursouto.mapper.AssetMapper;
import com.arthursouto.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Each row is written in its own REQUIRES_NEW transaction so that a constraint
 * violation on one row can never roll back the rows already committed before it.
 */
@Service
@RequiredArgsConstructor
class AssetImportRowWriter {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    enum Result { CREATED, UPDATED }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Result upsertRow(AssetRequest request) {
        try {
            return assetRepository.findByCode(request.code())
                    .map(existing -> {
                        assetMapper.updateFromImport(request, existing);
                        assetRepository.save(existing);
                        return Result.UPDATED;
                    })
                    .orElseGet(() -> {
                        assetRepository.save(assetMapper.toEntity(request));
                        return Result.CREATED;
                    });
        } catch (DataIntegrityViolationException e) {
            throw new AssetImportRowException("Database constraint violation: " + e.getMostSpecificCause().getMessage());
        }
    }
}
