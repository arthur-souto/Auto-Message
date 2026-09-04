package com.arthursouto.service;

import com.arthursouto.domain.Asset;
import com.arthursouto.domain.Doctor;
import com.arthursouto.domain.Formula;
import com.arthursouto.domain.FormulaItem;
import com.arthursouto.domain.Patient;
import com.arthursouto.domain.User;
import com.arthursouto.dto.DoctorResponse;
import com.arthursouto.dto.FormulaItemRequest;
import com.arthursouto.dto.FormulaItemResponse;
import com.arthursouto.dto.FormulaRequest;
import com.arthursouto.dto.FormulaResponse;
import com.arthursouto.dto.IncompatibilityWarningResponse;
import com.arthursouto.dto.PatientResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.AssetIncompatibilityRepository;
import com.arthursouto.repository.AssetRepository;
import com.arthursouto.repository.DoctorRepository;
import com.arthursouto.repository.FormulaRepository;
import com.arthursouto.repository.PatientRepository;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaService {

    private final FormulaRepository formulaRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetIncompatibilityRepository assetIncompatibilityRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public Page<FormulaResponse> findAll(Pageable pageable) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        return formulaRepository.findAllByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FormulaResponse findById(UUID id) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        Formula formula = formulaRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Formula not found"));

        return toResponse(formula);
    }

    @Transactional
    public FormulaResponse create(FormulaRequest request) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);

        Doctor doctor = resolveDoctor(user, request.doctorId());
        Patient patient = resolvePatient(user, request.patientId());

        Formula formula = Formula.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .doctor(doctor)
                .patient(patient)
                .posology(request.posology())
                .quantity(request.quantity())
                .build();

        formula.getItems().addAll(buildItems(formula, request.items()));

        Formula saved = formulaRepository.save(formula);

        log.info("Created formula with ID: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public FormulaResponse update(UUID id, FormulaRequest request) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        Formula formula = formulaRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Formula not found"));

        formula.setName(request.name());
        formula.setDescription(request.description());
        formula.setDoctor(resolveDoctor(user, request.doctorId()));
        formula.setPatient(resolvePatient(user, request.patientId()));
        formula.setPosology(request.posology());
        formula.setQuantity(request.quantity());

        formula.getItems().clear();
        formula.getItems().addAll(buildItems(formula, request.items()));

        return toResponse(formulaRepository.save(formula));
    }

    @Transactional
    public void delete(UUID id) {
        User user = AuthenticatedUser.isAccountVerifiedAndReturn(userRepository);
        Formula formula = formulaRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Formula not found"));

        formulaRepository.delete(formula);
    }

    private Doctor resolveDoctor(User user, UUID doctorId) {
        if (doctorId == null) {
            return null;
        }

        return doctorRepository.findByIdAndUserId(doctorId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private Patient resolvePatient(User user, UUID patientId) {
        if (patientId == null) {
            return null;
        }

        return patientRepository.findByIdAndUserId(patientId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    private List<FormulaItem> buildItems(Formula formula, List<FormulaItemRequest> requests) {
        List<UUID> assetIds = requests.stream().map(FormulaItemRequest::assetId).toList();

        Map<UUID, Asset> assetsById = assetRepository.findAllById(assetIds).stream()
                .collect(Collectors.toMap(Asset::getId, asset -> asset));

        List<UUID> missingIds = assetIds.stream()
                .filter(assetId -> !assetsById.containsKey(assetId))
                .distinct()
                .toList();

        if (!missingIds.isEmpty()) {
            log.info("Assets not found for formula {}: {}", formula.getId(), missingIds);
            throw new ResourceNotFoundException("Assets not found: " + missingIds);
        }

        return requests.stream()
                .map(req -> FormulaItem.builder()
                        .formula(formula)
                        .asset(assetsById.get(req.assetId()))
                        .quantity(req.quantity())
                        .unit(req.unit())
                        .concentration(req.concentration())
                        .build())
                .toList();
    }

    private FormulaResponse toResponse(Formula formula) {
        List<FormulaItemResponse> items = formula.getItems().stream()
                .map(FormulaItemResponse::from)
                .toList();

        List<UUID> assetIds = formula.getItems().stream()
                .map(item -> item.getAsset().getId())
                .distinct()
                .toList();

        List<IncompatibilityWarningResponse> incompatibilities = assetIds.size() < 2
                ? List.of()
                : assetIncompatibilityRepository.findAllWithinAssetIds(assetIds).stream()
                        .map(incompatibility -> new IncompatibilityWarningResponse(
                                incompatibility.getAssetA().getId(),
                                incompatibility.getAssetA().getName(),
                                incompatibility.getAssetB().getId(),
                                incompatibility.getAssetB().getName(),
                                incompatibility.getReason()
                        ))
                        .toList();

        DoctorResponse doctor = formula.getDoctor() == null ? null : DoctorResponse.from(formula.getDoctor());
        PatientResponse patient = formula.getPatient() == null ? null : PatientResponse.from(formula.getPatient());

        return new FormulaResponse(
                formula.getId(),
                formula.getName(),
                formula.getDescription(),
                doctor,
                patient,
                formula.getPosology(),
                formula.getQuantity(),
                items,
                incompatibilities,
                formula.getCreatedAt(),
                formula.getUpdatedAt()
        );
    }
}
