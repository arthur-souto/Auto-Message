package com.arthursouto.factory;

import com.arthursouto.domain.User;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class UserFactory {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private UserFactory() {
    }

    public static User.UserBuilder<?, ?> userBuilder() {
        int n = SEQUENCE.incrementAndGet();
        Instant now = Instant.now();
        return User.builder()
                .id(UUID.randomUUID())
                .email("user" + n + "@example.com")
                .name("Test User " + n)
                .username("testuser" + n)
                .googleId("google-id-" + n)
                .profileImage("https://example.com/avatar/" + n + ".png")
                .createdAt(now)
                .updatedAt(now);
    }

    public static User user() {
        return userBuilder().build();
    }

    public static User userWithoutGoogleId() {
        return userBuilder().googleId(null).build();
    }
}
