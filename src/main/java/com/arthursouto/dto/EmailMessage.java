package com.arthursouto.dto;


import java.util.Map;

public record EmailMessage(
        String to,
        String subject,
        String templatePath,
        Map<String, String> vars
) {
}
