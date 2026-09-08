package com.arthursouto.dto;

public record TokenPairResponse(
        String accessToken,
        String refreshToken
) {}
