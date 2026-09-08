package com.arthursouto.service;

import com.arthursouto.domain.Patient;
import com.arthursouto.domain.User;
import com.arthursouto.dto.PatientRequest;
import com.arthursouto.dto.PatientResponse;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.PatientRepository;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(Pageable pageable) {
        User user = AuthenticatedUser.user(userRepository);
        return patientRepository.findAllByUserId(user.getId(), pageable).map(PatientResponse::from);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(UUID id) {
        User user = AuthenticatedUser.user(userRepository);
        Patient patient = patientRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return PatientResponse.from(patient);
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        User user = AuthenticatedUser.user(userRepository);

        Patient patient = Patient.builder()
                .user(user)
                .name(request.name())
                .document(request.document())
                .birthDate(request.birthDate())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .notes(request.notes())
                .build();

        try {
            return PatientResponse.from(patientRepository.save(patient));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A patient with this document is already registered");
        }
    }

    @Transactional
    public PatientResponse update(UUID id, PatientRequest request) {
        User user = AuthenticatedUser.user(userRepository);
        Patient patient = patientRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patient.setName(request.name());
        patient.setDocument(request.document());
        patient.setBirthDate(request.birthDate());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setNotes(request.notes());

        try {
            patientRepository.saveAndFlush(patient);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A patient with this document is already registered");
        }

        return PatientResponse.from(patient);
    }

    @Transactional
    public void delete(UUID id) {
        User user = AuthenticatedUser.user(userRepository);
        Patient patient = patientRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        try {
            patientRepository.delete(patient);
            patientRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Patient is used in an existing formula and cannot be deleted");
        }
    }
}
