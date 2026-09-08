package com.arthursouto.factory;

import com.arthursouto.domain.Formula;
import com.arthursouto.domain.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class FormulaFactory {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private FormulaFactory() {
    }

    public static Formula.FormulaBuilder formulaBuilder(User user) {
        int n = SEQUENCE.incrementAndGet();
        Instant now = Instant.now();
        return Formula.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name("Formula " + n)
                .items(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now);
    }

    public static Formula formula(User user) {
        return formulaBuilder(user).build();
    }
}
