package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class  MessageService {

    public void sendWelcomeMessage(User user) {
        new EmailMessage(
                user.getEmail(),
                "Welcome to my application" + " " + user.getUsername(),
                "email/welcome",
                Map.of(
                        "name", user.getName()
                )
        );
    }


}
