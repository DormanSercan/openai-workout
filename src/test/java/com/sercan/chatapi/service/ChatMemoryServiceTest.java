package com.sercan.chatapi.service;

import com.sercan.chatapi.model.ChatMessage;
import com.sercan.chatapi.model.ChatRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatMemoryServiceTest {

    @Test
    void updateMemory_shouldStoreUserAndAssistantMessages() {
        ChatMemoryService chatMemoryService = new ChatMemoryService(10);

        chatMemoryService.updateMemory(
                "session-1",
                "Hello",
                "Hi, how can I help?"
        );

        List<ChatMessage> history = chatMemoryService.getHistory("session-1");

        assertEquals(4, history.size());

        assertEquals(ChatRole.USER, history.get(0).getRole());
        assertEquals("Hello", history.get(0).getContent());

        assertEquals(ChatRole.ASSISTANT, history.get(1).getRole());
        assertEquals("Hi, how can I help?", history.get(1).getContent());
    }

    @Test
    void getHistory_shouldKeepSessionsSeparated() {
        ChatMemoryService chatMemoryService = new ChatMemoryService(10);

        chatMemoryService.updateMemory(
                "session-1",
                "Message from session 1",
                "Reply to session 1"
        );

        chatMemoryService.updateMemory(
                "session-2",
                "Message from session 2",
                "Reply to session 2"
        );

        List<ChatMessage> session1History = chatMemoryService.getHistory("session-1");
        List<ChatMessage> session2History = chatMemoryService.getHistory("session-2");

        assertEquals("Message from session 1", session1History.get(0).getContent());
        assertEquals("Message from session 2", session2History.get(0).getContent());
    }

    @Test
    void updateMemory_shouldTrimHistoryWhenLimitExceeded() {
        ChatMemoryService chatMemoryService = new ChatMemoryService(4);

        chatMemoryService.updateMemory("session-1", "User 1", "Assistant 1");
        chatMemoryService.updateMemory("session-1", "User 2", "Assistant 2");
        chatMemoryService.updateMemory("session-1", "User 3", "Assistant 3");

        List<ChatMessage> history = chatMemoryService.getHistory("session-1");

        assertEquals(4, history.size());

        assertEquals("User 2", history.get(0).getContent());
        assertEquals("Assistant 2", history.get(1).getContent());
        assertEquals("User 3", history.get(2).getContent());
        assertEquals("Assistant 3", history.get(3).getContent());
    }
}