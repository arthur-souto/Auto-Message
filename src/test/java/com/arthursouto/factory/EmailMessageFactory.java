package com.arthursouto.factory;

import com.arthursouto.dto.EmailMessage;

import java.util.Map;

public final class EmailMessageFactory {

    private EmailMessageFactory() {
    }

    public static EmailMessage welcomeMessage(String to, String name) {
        return new EmailMessage(to, "Welcome", "email/welcome", Map.of("name", name));
    }

    public static EmailMessage of(String to) {
        return welcomeMessage(to, "Test User");
    }
}
