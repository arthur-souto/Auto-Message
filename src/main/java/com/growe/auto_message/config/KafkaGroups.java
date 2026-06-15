package com.growe.auto_message.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaGroups {
    // group for identification of consumer, messages are sent to one of the consumers in the same group
    public static final String MESSAGE_DEFAULT_GROUP = "auto-message-group-1";
}
