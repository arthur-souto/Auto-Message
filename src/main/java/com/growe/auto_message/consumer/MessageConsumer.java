package com.growe.auto_message.consumer;

import com.growe.auto_message.config.KafkaGroups;
import com.growe.auto_message.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {
    // topic = subscribe in MESSAGE_DEFAULT_TOPIC
    /*
      groupId = entry in MESSAGE_DEFAULT_GROUP,
      if there are multiple consumers with the same groupId,
      messages will be sent to one of the consumers in the same group
    */
    @KafkaListener(topics = KafkaTopics.MESSAGE_DEFAULT_TOPIC, groupId = KafkaGroups.MESSAGE_DEFAULT_GROUP)
    public void receiveMessage(String message) {
        log.info("Received message: {}", message);
        log.info("Operation successfully");
    }
}
