package com.arthursouto.controller;

import com.arthursouto.domain.User;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.JwtService;
import com.arthursouto.service.RefreshTokenService;
import com.arthursouto.service.UserService;
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

    public record RefreshRequest(String refreshToken) {}

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


    @PutMapping("/active")
    public void activeMe(@AuthenticationPrincipal UUID userId, @RequestParam String secret) {
        userService.activeUser(userId, secret);
    }

    @GetMapping("/me")
    public MeResponse getMe(@AuthenticationPrincipal UUID userId) {
        return userService.getUserById(userId);
    }
}
