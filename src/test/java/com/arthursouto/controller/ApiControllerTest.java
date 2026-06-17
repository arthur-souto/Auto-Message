package com.arthursouto.controller;

import com.arthursouto.dto.SendMessageRequest;
import com.arthursouto.repository.UserRepository;
import com.arthursouto.service.JwtService;
import com.arthursouto.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    // JwtAuthFilter is picked up by @WebMvcTest's Filter-bean scan even though it's excluded
    // from the MockMvc chain via addFilters=false, so its constructor deps still need beans.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void sendDelegatesToServiceAndReturnsConfirmation() throws Exception {
        when(messageService.sendMessage(new SendMessageRequest("hello"))).thenReturn("Message send successfully");

        mockMvc.perform(post("/v1/api/send")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SendMessageRequest("hello"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Message send successfully"));
    }
}
