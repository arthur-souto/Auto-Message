package com.growe.auto_message.service;

import com.growe.auto_message.dto.SendMessageRequest;
import com.growe.auto_message.producer.MessageProducer;
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
