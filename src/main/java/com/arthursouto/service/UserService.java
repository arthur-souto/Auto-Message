package com.arthursouto.service;

import com.arthursouto.dto.MeResponse;
import com.arthursouto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MeResponse getUserById(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new BadCredentialsException("invalid credentials"));
        return MeResponse.from(user);
    }
}
