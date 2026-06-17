package com.arthursouto.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private SpringTemplateEngine templateEngine;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(javaMailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "from", "no-reply@example.com");
    }

    @Test
    void sendsRenderedHtmlEmailToRecipient() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any(Context.class))).thenReturn("<p>Hi Jane</p>");

        emailService.send("jane@example.com", "Welcome", "email/welcome", Map.of("name", "Jane"));

        verify(javaMailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Welcome");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("jane@example.com");
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo("no-reply@example.com");
    }
}
