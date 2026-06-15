package com.arthursouto.service;

import com.arthursouto.dto.SendMessageRequest;
import com.arthursouto.producer.MessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageProducer messageProducer;

    public String sendMessage(SendMessageRequest req) {
        messageProducer.sendMessage(req.message());
        return "Message send successfully";
    }
}
