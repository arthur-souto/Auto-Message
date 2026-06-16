package com.arthursouto.controller;

import com.arthursouto.config.AuthCodeCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/auth")
public class AuthExchangeController {

    private final AuthCodeCache authCodeCache;

    public record ExchangeRequest(String code) {}

    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(@RequestBody ExchangeRequest request) {
        return authCodeCache.consume(request.code())
                .<ResponseEntity<?>>map(tokens -> ResponseEntity.ok(Map.of(
                        "accessToken", tokens.accessToken(),
                        "refreshToken", tokens.refreshToken()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid code or expired")));
    }

}
