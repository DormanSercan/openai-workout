package com.sercan.chatapi.service;

import com.sercan.chatapi.config.PromptProperties;
import com.sercan.chatapi.dto.ai.ChatRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderServiceTest {

    @Test
    void buildPrompt_shouldIncludeNoHistoryMessage_whenHistoryIsEmpty() {
        ChatMemoryService chatMemoryService = new ChatMemoryService(10);

        PromptProperties promptProperties = new PromptProperties();
        promptProperties.setSystem("You are a backend mentor.");
        promptProperties.setNoHistoryMessage("Conversation history for this session: NONE");

        PromptBuilderService promptBuilderService =
                new PromptBuilderService(chatMemoryService, promptProperties);

        ChatRequest request = new ChatRequest();
        request.setSessionId("session-1");
        request.setMessage("Explain REST API.");

        String prompt = promptBuilderService.buildPrompt(request);

        assertTrue(prompt.contains("You are a backend mentor."));
        assertTrue(prompt.contains("Conversation history for this session: NONE"));
        assertTrue(prompt.contains("Explain REST API."));
    }

    @Test
    void buildPrompt_shouldIncludeConversationHistory_whenHistoryExists() {
        ChatMemoryService chatMemoryService = new ChatMemoryService(10);

        chatMemoryService.updateMemory(
                "session-1",
                "What is dependency injection?",
                "It is a way to provide dependencies from outside."
        );

        PromptProperties promptProperties = new PromptProperties();
        promptProperties.setSystem("You are a backend mentor.");
        promptProperties.setNoHistoryMessage("Conversation history for this session: NONE");

        PromptBuilderService promptBuilderService =
                new PromptBuilderService(chatMemoryService, promptProperties);

        ChatRequest request = new ChatRequest();
        request.setSessionId("session-1");
        request.setMessage("Give a Spring Boot example.");

        String prompt = promptBuilderService.buildPrompt(request);

        assertTrue(prompt.contains("user: What is dependency injection?"));
        assertTrue(prompt.contains("assistant: It is a way to provide dependencies from outside."));
        assertTrue(prompt.contains("Give a Spring Boot example."));
    }
}