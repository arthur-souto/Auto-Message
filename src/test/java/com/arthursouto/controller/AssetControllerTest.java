package com.arthursouto.controller;

import com.arthursouto.dto.AssetBulkDeleteRequest;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.AssetService;
import com.arthursouto.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssetService assetService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deleteAssetReturnsNoContentWhenAssetExists() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/v1/api/assets/{id}", id))
                .andExpect(status().isNoContent());

        verify(assetService).deleteAsset(id);
    }

    @Test
    void deleteAssetReturnsNotFoundWhenAssetDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Asset not found")).when(assetService).deleteAsset(id);

        mockMvc.perform(delete("/v1/api/assets/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Asset not found"));
    }

    @Test
    void deleteAssetsReturnsNoContentWhenAllIdsExist() throws Exception {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(delete("/v1/api/assets/batch")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AssetBulkDeleteRequest(ids))))
                .andExpect(status().isNoContent());

        verify(assetService).deleteAssets(ids);
    }

    @Test
    void deleteAssetsReturnsNotFoundWhenSomeIdsAreMissing() throws Exception {
        List<UUID> ids = List.of(UUID.randomUUID());
        doThrow(new ResourceNotFoundException("Assets not found: " + ids))
                .when(assetService).deleteAssets(ids);

        mockMvc.perform(delete("/v1/api/assets/batch")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AssetBulkDeleteRequest(ids))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAssetsReturnsBadRequestWhenIdsIsEmpty() throws Exception {
        mockMvc.perform(delete("/v1/api/assets/batch")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AssetBulkDeleteRequest(List.of()))))
                .andExpect(status().isBadRequest());
    }
}
