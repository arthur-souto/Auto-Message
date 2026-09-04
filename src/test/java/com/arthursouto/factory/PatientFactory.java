package com.arthursouto.factory;

import com.arthursouto.domain.Patient;
import com.arthursouto.domain.User;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class PatientFactory {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private PatientFactory() {
    }

    public static Patient.PatientBuilder patientBuilder(User user) {
        int n = SEQUENCE.incrementAndGet();
        Instant now = Instant.now();
        return Patient.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name("Paciente Fulano " + n)
                .document("000.000.000-" + (10 + n))
                .createdAt(now)
                .updatedAt(now);
    }

    public static Patient patient(User user) {
        return patientBuilder(user).build();
    }
}
