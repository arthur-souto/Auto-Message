package com.arthursouto.service;

import com.arthursouto.domain.Doctor;
import com.arthursouto.domain.User;
import com.arthursouto.dto.DoctorRequest;
import com.arthursouto.dto.DoctorResponse;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.DoctorFactory;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.DoctorRepository;
import com.arthursouto.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoctorService doctorService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private User authenticateAsVerifiedUser() {
        User user = UserFactory.userBuilder().isVerified(true).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null)
        );
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void createSavesDoctorForAuthenticatedUser() {
        authenticateAsVerifiedUser();
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorResponse response = doctorService.create(new DoctorRequest("Dra. Maria", "CRM/SP 123456", "Endocrinologia"));

        assertThat(response.name()).isEqualTo("Dra. Maria");
        assertThat(response.crm()).isEqualTo("CRM/SP 123456");
    }

    @Test
    void createThrowsConflictWhenCrmAlreadyRegistered() {
        authenticateAsVerifiedUser();
        when(doctorRepository.save(any(Doctor.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> doctorService.create(new DoctorRequest("Dra. Maria", "CRM/SP 123456", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findByIdThrowsWhenDoctorDoesNotBelongToUser() {
        User user = authenticateAsVerifiedUser();
        UUID doctorId = UUID.randomUUID();
        when(doctorRepository.findByIdAndUserId(doctorId, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.findById(doctorId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteThrowsConflictWhenDoctorIsUsedInFormula() {
        User user = authenticateAsVerifiedUser();
        Doctor doctor = DoctorFactory.doctor(user);
        when(doctorRepository.findByIdAndUserId(doctor.getId(), user.getId())).thenReturn(Optional.of(doctor));
        doThrow(new DataIntegrityViolationException("fk violation")).when(doctorRepository).flush();

        assertThatThrownBy(() -> doctorService.delete(doctor.getId()))
                .isInstanceOf(ConflictException.class);

        verify(doctorRepository).delete(doctor);
    }
}
