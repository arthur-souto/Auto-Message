package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.dto.UserUpdateRequest;
import com.arthursouto.exception.BadRequestException;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.mapper.UserMapper;
import com.arthursouto.repository.UserRepository;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecureRandom secureRandom;
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;

    @Value("${app.activation.secret}")
    private String privateSecret;

    @Transactional(readOnly = true)
    public MeResponse getUserById(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return MeResponse.from(user);
    }

    @Transactional
    public void startVerification(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final var code = generateUserCodeVerification();

        try {
            sendCodeToEmail(code, user);
            verificationCodeService.save(user.getId(), code);
            log.info("send code to {}", user.getEmail());
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public void activeUser(UUID userId, String code) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String redisCode = verificationCodeService.getCode(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification code not found"));

        if(!redisCode.equals(code)) {
            throw new BadRequestException("Wrong code");
        }

        user.setVerified(true);
        userRepository.save(user);
        verificationCodeService.invalidateCode(userId);
    }


    private void sendCodeToEmail(String code, User user) throws MessagingException {
        emailService.send(
                user.getEmail(),
                "Seu código de verificação",
                "email/verification-code",
                Map.of(
                        "name", user.getName(),
                        "code", code,
                        "expiresInMinutes", String.valueOf(verificationCodeService.getExpiration().toMinutes())
                )
        );
    }

    private String generateUserCodeVerification() {
        byte[] randomBytes = new byte[4];
        secureRandom.nextBytes(randomBytes);
        return BaseEncoding.base32().omitPadding().encode(randomBytes).substring(0, 6);
    }

    @Transactional
    public MeResponse updateUser(UUID userId, UserUpdateRequest request) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userMapper.updateUser(request, user);

        return MeResponse.from(userRepository.save(user));
    }
}
