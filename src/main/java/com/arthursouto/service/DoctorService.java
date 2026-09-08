package com.arthursouto.service;

import com.arthursouto.domain.Doctor;
import com.arthursouto.domain.User;
import com.arthursouto.dto.DoctorRequest;
import com.arthursouto.dto.DoctorResponse;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.DoctorRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<DoctorResponse> findAll(Pageable pageable) {
        User user = AuthenticatedUser.user(userRepository);
        return doctorRepository.findAllByUserId(user.getId(), pageable).map(DoctorResponse::from);
    }

    @Transactional(readOnly = true)
    public DoctorResponse findById(UUID id) {
        User user = AuthenticatedUser.user(userRepository);
        Doctor doctor = doctorRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return DoctorResponse.from(doctor);
    }

    @Transactional
    public DoctorResponse create(DoctorRequest request) {
        User user = AuthenticatedUser.user(userRepository);

        Doctor doctor = Doctor.builder()
                .user(user)
                .name(request.name())
                .crm(request.crm())
                .specialty(request.specialty())
                .build();

        try {
            return DoctorResponse.from(doctorRepository.save(doctor));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A doctor with this CRM is already registered");
        }
    }

    @Transactional
    public DoctorResponse update(UUID id, DoctorRequest request) {
        User user = AuthenticatedUser.user(userRepository);
        Doctor doctor = doctorRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        doctor.setName(request.name());
        doctor.setCrm(request.crm());
        doctor.setSpecialty(request.specialty());

        try {
            doctorRepository.saveAndFlush(doctor);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A doctor with this CRM is already registered");
        }

        return DoctorResponse.from(doctor);
    }

    @Transactional
    public void delete(UUID id) {
        User user = AuthenticatedUser.user(userRepository);
        Doctor doctor = doctorRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        try {
            doctorRepository.delete(doctor);
            doctorRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Doctor is used in an existing formula and cannot be deleted");
        }
    }
}
