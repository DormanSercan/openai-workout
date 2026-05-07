package com.sercan.chatapi.service;

import com.sercan.chatapi.config.PromptProperties;
import com.sercan.chatapi.dto.ai.ChatRequest;
import com.sercan.chatapi.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptBuilderService {

    private final ChatMemoryService chatMemoryService;
    private final PromptProperties promptProperties;

    public PromptBuilderService(ChatMemoryService chatMemoryService,
                                PromptProperties promptProperties) {
        this.chatMemoryService = chatMemoryService;
        this.promptProperties = promptProperties;
    }

    public String buildPrompt(ChatRequest request) {
        List<ChatMessage> history = chatMemoryService.getHistory(request.getSessionId());

        StringBuilder prompt = new StringBuilder();

        prompt.append(promptProperties.getSystem()).append("\n\n");

        if (history.isEmpty()) {
            prompt.append(promptProperties.getNoHistoryMessage()).append("\n\n");
        } else {
            prompt.append("Conversation history for this session:\n");

            for (ChatMessage message : history) {
                prompt.append(message.getRole().name().toLowerCase())
                        .append(": ")
                        .append(message.getContent())
                        .append("\n");
            }

            prompt.append("\n");
        }

        prompt.append("Current user message:\n");
        prompt.append(request.getMessage()).append("\n");
        prompt.append("Assistant:");

        return prompt.toString();
    }
}