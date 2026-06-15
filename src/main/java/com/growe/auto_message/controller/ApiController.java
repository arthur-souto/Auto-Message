package com.growe.auto_message.controller;

import com.growe.auto_message.dto.SendMessageRequest;
import com.growe.auto_message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class ApiController {

    private final MessageService messageService;

    @PostMapping("/send")
    public String sendMessage(@RequestBody SendMessageRequest req) {
        return messageService.sendMessage(req);
    }
}
