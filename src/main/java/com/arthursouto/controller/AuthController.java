package com.arthursouto.controller;

import com.arthursouto.domain.User;
import com.arthursouto.dto.CreateUserRequest;
import com.arthursouto.dto.LoginRequest;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.dto.TokenPairResponse;
import com.arthursouto.dto.UserUpdateRequest;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.AuthService;
import com.arthursouto.service.JwtService;
import com.arthursouto.service.RefreshTokenService;
import com.arthursouto.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthService authService;

    public record RefreshRequest(String refreshToken) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenPairResponse register(@Valid @RequestBody CreateUserRequest request) {
        return authService.createUserAccount(request);
    }

    @PostMapping("/login")
    public TokenPairResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String ,String>> refresh(@RequestBody RefreshRequest req) {
        return refreshTokenService.rotate(req.refreshToken())
                .map(rotation -> {
                   User user = userRepository.findById(rotation.userId())
                           .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                   String newAccessToken = jwtService.generateToken(user);

                   return ResponseEntity.ok(Map.of(
                           "accessToken", newAccessToken,
                           "refreshToken", rotation.newToken()
                   ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token invalid or expired"))
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verification-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendVerificationCode(@AuthenticationPrincipal UUID userId) {
        userService.startVerification(userId);
    }

    @PutMapping("/active")
    public void activeMe(@AuthenticationPrincipal UUID userId, @RequestParam String code) {
        userService.activeUser(userId, code);
    }

    @GetMapping("/me")
    public MeResponse getMe(@AuthenticationPrincipal UUID userId) {
        return userService.getUserById(userId);
    }

    @PatchMapping("/me")
    public MeResponse updateMe(@AuthenticationPrincipal UUID userId, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }
}
