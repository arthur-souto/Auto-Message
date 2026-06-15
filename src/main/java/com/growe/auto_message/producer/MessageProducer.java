package com.growe.auto_message.producer;

import com.growe.auto_message.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProducer {
    // <topic, message>
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        kafkaTemplate.send(KafkaTopics.MESSAGE_DEFAULT_TOPIC, message);
    }
}
