package com.arthursouto.filters;

import com.arthursouto.repository.UserRepository;
import com.arthursouto.utils.JsonErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifiedAccountFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    private final List<RequestMatcher> excludeMatchers = List.of(
            PathPatternRequestMatcher.withDefaults().matcher("/api/public/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/api/auth/login"),
            PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**")
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        return excludeMatchers.stream().anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof UUID userId) {

            log.info("Checking if user {} is verified", userId);

            boolean isVerified = userRepository.isVerifiedById(userId);

            if(!isVerified) {
                log.info("User {} is not verified, returning 401", userId);
                JsonErrorWriter.write(
                        request,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "Account unverified, please check if your account is active"
                );
                return;
            }

            log.info("User {} is verified, proceeding with request", userId);
        }

        filterChain.doFilter(request, response);

    }
}
