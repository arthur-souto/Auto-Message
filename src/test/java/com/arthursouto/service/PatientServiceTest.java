package com.arthursouto.service;

import com.arthursouto.domain.Patient;
import com.arthursouto.domain.User;
import com.arthursouto.dto.PatientRequest;
import com.arthursouto.dto.PatientResponse;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.factory.PatientFactory;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.repository.PatientRepository;
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
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

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
    void createSavesPatientForAuthenticatedUser() {
        authenticateAsVerifiedUser();
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = patientService.create(
                new PatientRequest("Maria Silva", "123.456.789-00", null, "11999990000", "maria@email.com", null, null)
        );

        assertThat(response.name()).isEqualTo("Maria Silva");
        assertThat(response.document()).isEqualTo("123.456.789-00");
    }

    @Test
    void createThrowsConflictWhenDocumentAlreadyRegistered() {
        authenticateAsVerifiedUser();
        when(patientRepository.save(any(Patient.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> patientService.create(
                new PatientRequest("Maria Silva", "123.456.789-00", null, null, null, null, null)
        )).isInstanceOf(ConflictException.class);
    }

    @Test
    void findByIdThrowsWhenPatientDoesNotBelongToUser() {
        User user = authenticateAsVerifiedUser();
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findByIdAndUserId(patientId, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(patientId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteThrowsConflictWhenPatientIsUsedInFormula() {
        User user = authenticateAsVerifiedUser();
        Patient patient = PatientFactory.patient(user);
        when(patientRepository.findByIdAndUserId(patient.getId(), user.getId())).thenReturn(Optional.of(patient));
        doThrow(new DataIntegrityViolationException("fk violation")).when(patientRepository).flush();

        assertThatThrownBy(() -> patientService.delete(patient.getId()))
                .isInstanceOf(ConflictException.class);

        verify(patientRepository).delete(patient);
    }
}
