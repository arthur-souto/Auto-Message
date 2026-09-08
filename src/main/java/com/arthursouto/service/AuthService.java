package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.CreateUserRequest;
import com.arthursouto.dto.LoginRequest;
import com.arthursouto.dto.TokenPairResponse;
import com.arthursouto.dto.UserPlanRequest;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.issuer.RefreshTokenIssuer;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.rules.PlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final UserPlanService userPlanService;

    @Value("${app.legal.terms-version}")
    private String termsVersion;

    private static final Instant EXPIRATION_DEFAULT_DATE = Instant.now().atOffset(ZoneOffset.UTC).plusMonths(3).toInstant();

    @Transactional
    public TokenPairResponse createUserAccount(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already used");
        }

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .username(request.username())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .acceptedTermsAt(Instant.now())
                .acceptedTermsVersion(termsVersion)
                .build();

        final var saved = userRepository.save(user);

        userPlanService.assignUser(
                new UserPlanRequest(
                        PlanType.FREE,
                        EXPIRATION_DEFAULT_DATE

                ),
                user
        );

        final var token = jwtService.generateToken(saved);
        final var refreshToken = refreshTokenIssuer.generate(saved);

        return new TokenPairResponse(
                token,
                refreshToken
        );
    }

    public TokenPairResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(user.getDeletedAt() != null) {
            throw new UnauthorizedException("This account has been deleted");
        }

        if(!bCryptPasswordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("The password is incorrect");
        };

        final var token = jwtService.generateToken(user);
        final var refreshToken = refreshTokenIssuer.generate(user);

        return new TokenPairResponse(
                token,
                refreshToken
        );
    }
}
