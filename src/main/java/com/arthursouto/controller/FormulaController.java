package com.arthursouto.controller;

import com.arthursouto.dto.FormulaRequest;
import com.arthursouto.dto.FormulaResponse;
import com.arthursouto.service.FormulaPdfService;
import com.arthursouto.service.FormulaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/formulas")
@RequiredArgsConstructor
public class FormulaController {

    private final FormulaService formulaService;
    private final FormulaPdfService formulaPdfService;

    @GetMapping
    public Page<FormulaResponse> findAll(Pageable pageable) {
        return formulaService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public FormulaResponse findById(@PathVariable UUID id) {
        return formulaService.findById(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID id) {
        byte[] pdf = formulaPdfService.generatePdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"formula-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FormulaResponse create(@Valid @RequestBody FormulaRequest request) {
        return formulaService.create(request);
    }

    @PutMapping("/{id}")
    public FormulaResponse update(@PathVariable UUID id, @Valid @RequestBody FormulaRequest request) {
        return formulaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        formulaService.delete(id);
    }
}
