package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.CreateUserRequest;
import com.arthursouto.dto.LoginRequest;
import com.arthursouto.dto.TokenPairResponse;
import com.arthursouto.exception.ConflictException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.issuer.RefreshTokenIssuer;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenIssuer refreshTokenIssuer;

    public TokenPairResponse createUserAccount(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already used");
        }

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .username(request.username())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .build();

        final var saved = userRepository.save(user);

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
