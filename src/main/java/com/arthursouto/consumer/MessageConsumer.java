package com.arthursouto.consumer;

import com.arthursouto.config.KafkaGroups;
import com.arthursouto.config.KafkaTopics;
import com.arthursouto.dto.EmailMessage;
import com.arthursouto.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    // topic = subscribe in MESSAGE_DEFAULT_TOPIC
    /*
      groupId = entry in MESSAGE_DEFAULT_GROUP,
      if there are multiple consumers with the same groupId,
      messages will be sent to one of the consumers in the same group

    */
    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "false",
            include = MessagingException.class,
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = KafkaTopics.MESSAGE_DEFAULT_TOPIC, groupId = KafkaGroups.MESSAGE_DEFAULT_GROUP)
    public void receiveMessage(String raw) throws MessagingException {
        EmailMessage msg;
        try {
            msg = objectMapper.readValue(raw, EmailMessage.class);
        } catch (IOException e) {
            log.error("Failed to deserialize message: {}", raw, e);
            throw new IllegalStateException("Invalid Payload", e);
        }
        emailService.send(msg.to(), msg.subject(), msg.templatePath(), msg.vars());
    }

    @DltHandler
    public void handleDlt(String raw, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exMsg) {
        log.error("Mensagem foi para DLT após esgotar tentativas: {} | erro: {}", raw, exMsg);
        meterRegistry.counter("email.welcome.dlt.count").increment();
    }
}
