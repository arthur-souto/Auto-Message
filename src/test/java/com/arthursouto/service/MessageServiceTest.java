package com.arthursouto.service;

import com.arthursouto.domain.User;
import com.arthursouto.dto.EmailMessage;
import com.arthursouto.dto.SendMessageRequest;
import com.arthursouto.factory.SendMessageRequestFactory;
import com.arthursouto.factory.UserFactory;
import com.arthursouto.producer.MessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendMessagePublishesRawMessageAndReturnsConfirmation() {
        SendMessageRequest request = SendMessageRequestFactory.of("hello there");

        String result = messageService.sendMessage(request);

        assertThat(result).isEqualTo("Message send successfully");
        verify(messageProducer).sendMessage("hello there");
    }

    @Test
    void sendWelcomeMessageBuildsEmailWithUserDetails() {
        User user = UserFactory.userBuilder()
                .email("jane@example.com")
                .username("jane")
                .name("Jane Doe")
                .build();

        messageService.sendWelcomeMessage(user);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(messageProducer).sendMessage(captor.capture());

        EmailMessage sent = captor.getValue();
        assertThat(sent.to()).isEqualTo("jane@example.com");
        assertThat(sent.subject()).isEqualTo("Welcome to my application jane");
        assertThat(sent.templatePath()).isEqualTo("email/welcome");
        assertThat(sent.vars()).containsEntry("name", "Jane Doe");
    }
}
