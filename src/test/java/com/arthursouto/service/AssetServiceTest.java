package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.AssetFactory;
import com.arthursouto.mapper.AssetMapper;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetMapper assetMapper;

    @InjectMocks
    private AssetService assetService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsVerifiedUser() {
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null)
        );
        when(userRepository.isVerifiedById(userId)).thenReturn(true);
    }

    @Test
    void deleteAssetRemovesExistingAsset() {
        authenticateAsVerifiedUser();
        Asset asset = AssetFactory.asset();
        when(assetRepository.existsById(asset.getId())).thenReturn(true);

        assetService.deleteAsset(asset.getId());

        verify(assetRepository).deleteById(asset.getId());
    }

    @Test
    void deleteAssetThrowsWhenAssetNotFound() {
        authenticateAsVerifiedUser();
        UUID id = UUID.randomUUID();
        when(assetRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> assetService.deleteAsset(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Asset not found");

        verify(assetRepository, never()).deleteById(id);
    }

    @Test
    void deleteAssetsRemovesAllGivenIds() {
        authenticateAsVerifiedUser();
        Asset first = AssetFactory.asset();
        Asset second = AssetFactory.asset();
        List<UUID> ids = List.of(first.getId(), second.getId());
        when(assetRepository.findAllById(ids)).thenReturn(List.of(first, second));

        assetService.deleteAssets(ids);

        verify(assetRepository).deleteAllById(ids);
    }

    @Test
    void deleteAssetsThrowsWhenSomeIdsAreMissingAndDeletesNothing() {
        authenticateAsVerifiedUser();
        Asset found = AssetFactory.asset();
        UUID missingId = UUID.randomUUID();
        List<UUID> ids = List.of(found.getId(), missingId);
        when(assetRepository.findAllById(ids)).thenReturn(List.of(found));

        assertThatThrownBy(() -> assetService.deleteAssets(ids))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(missingId.toString());

        verify(assetRepository, never()).deleteAllById(anyList());
    }
}
