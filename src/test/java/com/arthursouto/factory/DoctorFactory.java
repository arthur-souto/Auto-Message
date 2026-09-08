package com.arthursouto.factory;

import com.arthursouto.domain.Doctor;
import com.arthursouto.domain.User;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class DoctorFactory {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private DoctorFactory() {
    }

    public static Doctor.DoctorBuilder doctorBuilder(User user) {
        int n = SEQUENCE.incrementAndGet();
        Instant now = Instant.now();
        return Doctor.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name("Dr. Fulano " + n)
                .crm("CRM/SP " + (100000 + n))
                .createdAt(now)
                .updatedAt(now);
    }

    public static Doctor doctor(User user) {
        return doctorBuilder(user).build();
    }
}
