package com.arthursouto.service;

import com.arthursouto.dto.MeResponse;
import com.arthursouto.dto.UserUpdateRequest;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.exception.UnauthorizedException;
import com.arthursouto.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @Value("${app.activation.secret}")
    private String privateSecret;

    @Transactional(readOnly = true)
    public MeResponse getUserById(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return MeResponse.from(user);
    }

    @Transactional
    public void activeUser(UUID userId, String secret) {

        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!privateSecret.equals(secret)) {
            throw new UnauthorizedException("Wrong or Invalid [Secret]");
        }

        user.setVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public MeResponse updateUser(UUID userId, UserUpdateRequest request) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userMapper.updateUser(request, user);

        return MeResponse.from(userRepository.save(user));
    }
}
