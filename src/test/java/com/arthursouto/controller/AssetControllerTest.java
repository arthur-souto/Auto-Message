package com.arthursouto.controller;

import com.arthursouto.dto.AssetBulkDeleteRequest;
import com.arthursouto.dto.AssetImportResponse;
import com.arthursouto.dto.AssetImportRowError;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.AssetImportService;
import com.arthursouto.service.AssetService;
import com.arthursouto.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    private AssetImportService assetImportService;

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

    @Test
    void importAssetsReturnsReportWithRowErrors() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "assets.csv", "text/csv", "code,name\nA1,Asset One\n".getBytes(StandardCharsets.UTF_8));

        AssetImportResponse response = new AssetImportResponse(
                1, 1, 0, 0, List.of());
        when(assetImportService.importAssets(any())).thenReturn(response);

        mockMvc.perform(multipart("/v1/api/assets/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(1))
                .andExpect(jsonPath("$.createdCount").value(1));
    }

    @Test
    void importAssetsReturnsBadRequestForUnsupportedFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "assets.txt", "text/plain", "not a spreadsheet".getBytes(StandardCharsets.UTF_8));

        doThrow(new BadRequestException("Unsupported file type. Expected .csv or .xlsx"))
                .when(assetImportService).importAssets(any());

        mockMvc.perform(multipart("/v1/api/assets/import").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported file type. Expected .csv or .xlsx"));
    }

    @Test
    void importAssetsReportIncludesRowLevelErrorsWithoutFailingTheRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "assets.csv", "text/csv", "code,name\nA1,\n".getBytes(StandardCharsets.UTF_8));

        AssetImportResponse response = new AssetImportResponse(
                1, 0, 0, 1, List.of(new AssetImportRowError(2, "A1", "name is required")));
        when(assetImportService.importAssets(any())).thenReturn(response);

        mockMvc.perform(multipart("/v1/api/assets/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failedCount").value(1))
                .andExpect(jsonPath("$.errors[0].rowNumber").value(2))
                .andExpect(jsonPath("$.errors[0].reason").value("name is required"));
    }

    @Test
    void downloadImportTemplateReturnsCsvAttachment() throws Exception {
        byte[] content = "code,name\n".getBytes(StandardCharsets.UTF_8);
        when(assetImportService.generateTemplate("csv")).thenReturn(content);

        mockMvc.perform(get("/v1/api/assets/import/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"asset-import-template.csv\""));

        verify(assetImportService).generateTemplate("csv");
    }

    @Test
    void downloadImportTemplateReturnsXlsxWhenFormatIsXlsx() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        when(assetImportService.generateTemplate("xlsx")).thenReturn(content);

        mockMvc.perform(get("/v1/api/assets/import/template").param("format", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"asset-import-template.xlsx\""));
    }
}
