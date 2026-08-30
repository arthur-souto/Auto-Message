package com.arthursouto.controller;

import com.arthursouto.dto.DoctorRequest;
import com.arthursouto.dto.DoctorResponse;
import com.arthursouto.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public Page<DoctorResponse> findAll(Pageable pageable) {
        return doctorService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public DoctorResponse findById(@PathVariable UUID id) {
        return doctorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) {
        return doctorService.create(request);
    }

    @PutMapping("/{id}")
    public DoctorResponse update(@PathVariable UUID id, @Valid @RequestBody DoctorRequest request) {
        return doctorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        doctorService.delete(id);
    }
}
