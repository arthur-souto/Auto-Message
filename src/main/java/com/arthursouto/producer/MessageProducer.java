package com.arthursouto.producer;

import com.arthursouto.config.KafkaTopics;
import com.arthursouto.dto.EmailMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProducer {
    // <topic, message>
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(String message) {
        kafkaTemplate.send(KafkaTopics.MESSAGE_DEFAULT_TOPIC, message);
    }

    public void sendMessage(EmailMessage emailMessage) {
        try {
            String json = objectMapper.writeValueAsString(emailMessage);
            kafkaTemplate.send(KafkaTopics.MESSAGE_DEFAULT_TOPIC, json);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize EmailMessage");
        }
    }
}
