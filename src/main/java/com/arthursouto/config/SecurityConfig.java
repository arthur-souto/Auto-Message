package com.arthursouto.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.frontend.base-url}")
    private String frontUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        String[] allowedPaths = {
                "/test/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/oauth2/**",
                "/login/**",
                "/public/**",
                "/actuator/health/**",
                "/v1/api/auth/exchange",
                "/v1/api/auth/refresh",
                "/v1/api/auth/logout"
        };

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(allowedPaths).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                )
                .oauth2Login(o -> o
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"" + exception.getMessage() + "\"}");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }


}
