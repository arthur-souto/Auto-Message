package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.domain.AssetIncompatibility;
import com.arthursouto.dto.AssetIncompatibilityRequest;
import com.arthursouto.dto.AssetIncompatibilityResponse;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.AssetFactory;
import com.arthursouto.repository.AssetIncompatibilityRepository;
import com.arthursouto.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetIncompatibilityServiceTest {

    @Mock
    private AssetIncompatibilityRepository assetIncompatibilityRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetIncompatibilityService assetIncompatibilityService;

    @Test
    void addIncompatibilityThrowsWhenSameAsset() {
        Asset asset = AssetFactory.asset();

        assertThatThrownBy(() -> assetIncompatibilityService.addIncompatibility(
                asset.getId(), new AssetIncompatibilityRequest(asset.getId(), "reason")))
                .isInstanceOf(BadRequestException.class);

        verify(assetIncompatibilityRepository, never()).save(any());
    }

    @Test
    void addIncompatibilityThrowsWhenPairAlreadyExists() {
        Asset asset = AssetFactory.asset();
        Asset other = AssetFactory.asset();

        when(assetRepository.findById(asset.getId())).thenReturn(Optional.of(asset));
        when(assetRepository.findById(other.getId())).thenReturn(Optional.of(other));
        when(assetIncompatibilityRepository.findByAssetPair(asset.getId(), other.getId()))
                .thenReturn(Optional.of(AssetIncompatibility.builder().build()));

        assertThatThrownBy(() -> assetIncompatibilityService.addIncompatibility(
                asset.getId(), new AssetIncompatibilityRequest(other.getId(), "reason")))
                .isInstanceOf(ConflictException.class);

        verify(assetIncompatibilityRepository, never()).save(any());
    }

    @Test
    void addIncompatibilitySavesPairAndReturnsOtherAssetFromRequesterPerspective() {
        Asset asset = AssetFactory.asset();
        Asset other = AssetFactory.asset();

        when(assetRepository.findById(asset.getId())).thenReturn(Optional.of(asset));
        when(assetRepository.findById(other.getId())).thenReturn(Optional.of(other));
        when(assetIncompatibilityRepository.findByAssetPair(asset.getId(), other.getId()))
                .thenReturn(Optional.empty());
        when(assetIncompatibilityRepository.save(any(AssetIncompatibility.class)))
                .thenAnswer(invocation -> {
                    AssetIncompatibility saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    saved.setCreatedAt(Instant.now());
                    return saved;
                });

        AssetIncompatibilityResponse response = assetIncompatibilityService.addIncompatibility(
                asset.getId(), new AssetIncompatibilityRequest(other.getId(), "Reduz absorção"));

        assertThat(response.assetId()).isEqualTo(other.getId());
        assertThat(response.reason()).isEqualTo("Reduz absorção");
    }

    @Test
    void deleteIncompatibilityThrowsWhenNotFound() {
        UUID assetId = UUID.randomUUID();
        UUID incompatibilityId = UUID.randomUUID();
        when(assetIncompatibilityRepository.findByIdAndInvolvingAsset(incompatibilityId, assetId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetIncompatibilityService.deleteIncompatibility(assetId, incompatibilityId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllByAssetIdThrowsWhenAssetNotFound() {
        UUID assetId = UUID.randomUUID();
        when(assetRepository.existsById(assetId)).thenReturn(false);

        assertThatThrownBy(() -> assetIncompatibilityService.findAllByAssetId(assetId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
