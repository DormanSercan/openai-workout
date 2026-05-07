package com.sercan.chatapi.service;

import com.sercan.chatapi.model.ChatMessage;
import com.sercan.chatapi.model.ChatRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMemoryService {

    private final int maxHistorySize;
    private final Map<String, List<ChatMessage>> memory = new ConcurrentHashMap<>();

    public ChatMemoryService(@Value("${chat.memory.max-history-size}") int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return new ArrayList<>(memory.getOrDefault(sessionId, new ArrayList<>()));
    }

    public void updateMemory(String sessionId, String userMessage, String assistantReply) {
        List<ChatMessage> history = getHistory(sessionId);

        history.add(new ChatMessage(ChatRole.USER, userMessage));
        history.add(new ChatMessage(ChatRole.ASSISTANT, assistantReply));

        if (history.size() > maxHistorySize) {
            history = history.subList(history.size() - maxHistorySize, history.size());
        }

        memory.put(sessionId, history);
    }
}