package com.arthursouto.controller;

import com.arthursouto.dto.AssetBulkDeleteRequest;
import com.arthursouto.dto.AssetImportResponse;
import com.arthursouto.dto.AssetResponse;
import com.arthursouto.dto.AssetUpdateRequest;
import com.arthursouto.dto.ConcentrationCheckResponse;
import com.arthursouto.service.AssetImportService;
import com.arthursouto.service.AssetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final AssetImportService assetImportService;

    @GetMapping("/search")
    public Page<AssetResponse> searchAssets(@RequestParam(required = false) String target, Pageable pageable) {
        return assetService.searchAssets(target, pageable);
    }

    @GetMapping("/{id}/concentration-check")
    public ConcentrationCheckResponse checkConcentration(
            @PathVariable UUID id,
            @RequestParam @DecimalMin("0.0") BigDecimal value) {
        return assetService.checkConcentration(id, value);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public AssetResponse updateAsset(@PathVariable UUID id, @RequestBody AssetUpdateRequest request) {
        return assetService.updateAsset(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable UUID id) {
        assetService.deleteAsset(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssets(@Valid @RequestBody AssetBulkDeleteRequest request) {
        assetService.deleteAssets(request.ids());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssetImportResponse importAssets(@RequestParam("file") MultipartFile file) {
        return assetImportService.importAssets(file);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadImportTemplate(@RequestParam(defaultValue = "csv") String format) {
        byte[] content = assetImportService.generateTemplate(format);
        String filename = "asset-import-template." + format.toLowerCase();
        MediaType mediaType = "xlsx".equalsIgnoreCase(format)
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.parseMediaType("text/csv");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
