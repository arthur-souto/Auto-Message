package com.arthursouto.factory;

import com.arthursouto.dto.SendMessageRequest;

public final class SendMessageRequestFactory {

    private SendMessageRequestFactory() {
    }

    public static SendMessageRequest of(String message) {
        return new SendMessageRequest(message);
    }

    public static SendMessageRequest defaultRequest() {
        return of("Hello, world!");
    }
}
