package com.arthursouto.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaTopics {

    // Topic, group of messages that are broadcasted to all subscribers
    public static final String MESSAGE_DEFAULT_TOPIC = "auto-message";
}
