package com.arthursouto.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void ofPopulatesAllFieldsWithCurrentTimestamp() {
        Instant before = Instant.now();

        ErrorResponse response = ErrorResponse.of(404, "Not Found", "User not found");

        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("User not found");
        assertThat(response.timestamp()).isBetween(before, Instant.now().plusSeconds(1));
    }
}
