package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.MeResponse;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Value("${app.activation.secret}")
    private String privateSecret;

    @Transactional(readOnly = true)
    public MeResponse getUserById(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return MeResponse.from(user);
    }

    @Transactional
    public void activeUser(User user, String secret) {
        if(!privateSecret.equals(secret)) {
            throw new UnauthorizedException("Wrong or Invalid [Secret]");
        }

        user.setVerified(true);
        userRepository.save(user);
    }
}
