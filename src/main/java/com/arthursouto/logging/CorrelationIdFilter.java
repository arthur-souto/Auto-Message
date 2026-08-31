package com.arthursouto.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Cria (ou reaproveita) um requestId por requisição e coloca no MDC do SLF4J.
 * Assim, se o pattern do logback tiver %X{requestId}, TODAS as linhas de log
 * geradas durante essa requisição — controller, service, repository, aspect —
 * aparecem com o mesmo id, o que facilita muito filtrar/rastrear no log.
 *
 * IMPORTANTE: esta classe NÃO é @Component de propósito. Ela é registrada
 * manualmente apenas dentro da cadeia do Spring Security (SecurityConfig),
 * via addFilterBefore. Se ela também fosse @Component, o Spring Boot a
 * registraria AUTOMATICAMENTE como um filtro genérico do servlet container
 * além do registro manual — rodando duas vezes por requisição e podendo
 * quebrar fluxos sensíveis a filtros, como o redirect do OAuth2 login.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Request-Id";
    private static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest req, @NonNull HttpServletResponse res,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestId = req.getHeader(HEADER_NAME);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        try {
            MDC.put(MDC_KEY, requestId);
            res.setHeader(HEADER_NAME, requestId);
            filterChain.doFilter(req, res);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}