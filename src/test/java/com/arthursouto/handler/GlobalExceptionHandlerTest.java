package com.arthursouto.handler;

import com.arthursouto.dto.ErrorResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAppSetsStatusAndBuildsErrorResponse() {
        HttpServletResponse response = new MockHttpServletResponse();
        var ex = new ResourceNotFoundException("User not found");

        ErrorResponse body = handler.handleApp(ex, response);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.message()).isEqualTo("User not found");
    }

    @Test
    void handleValidationReturnsFirstFieldError() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("request", "message", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        var ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ErrorResponse body = handler.handleValidation(ex);

        assertThat(body.status()).isEqualTo(400);
        assertThat(body.error()).isEqualTo("Bad Request");
        assertThat(body.message()).isEqualTo("message: must not be blank");
    }

    @Test
    void handleValidationFallsBackToDefaultMessageWhenNoFieldErrors() {
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        var ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ErrorResponse body = handler.handleValidation(ex);

        assertThat(body.message()).isEqualTo("Validation failed");
    }

    @Test
    void handleGenericReturnsInternalServerError() {
        ErrorResponse body = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message()).isEqualTo("An unexpected error occurred");
    }
}
