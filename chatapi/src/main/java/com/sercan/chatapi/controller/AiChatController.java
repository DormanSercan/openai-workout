package com.sercan.chatapi.controller;

import com.sercan.chatapi.dto.ai.ChatRequest;
import com.sercan.chatapi.dto.ai.ChatResponse;
import com.sercan.chatapi.service.OpenAiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final OpenAiService openAiService;

    public AiChatController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return openAiService.chat(request);
    }
}