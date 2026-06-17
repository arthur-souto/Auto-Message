package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.EmailMessage;
import com.arthursouto.dto.SendMessageRequest;
import com.arthursouto.producer.MessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageProducer messageProducer;

    public String sendMessage(SendMessageRequest req) {
        messageProducer.sendMessage(req.message());
        return "Message send successfully";
    }

    public void sendWelcomeMessage(User user) {
        final var message = new EmailMessage(
                user.getEmail(),
                "Welcome to my application" + " " + user.getUsername(),
                "email/welcome",
                Map.of(
                        "name", user.getName()
                )
        );
        messageProducer.sendMessage(message);
    }
}
