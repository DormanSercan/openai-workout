package com.sercan.chatapi.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMemoryService {

    private static final int MAX_HISTORY_SIZE = 10;

    private final Map<String, List<String>> memory = new ConcurrentHashMap<>();

    public List<String> getHistory(String sessionId) {
        return memory.getOrDefault(sessionId, new ArrayList<>());
    }

    public void updateMemory(String sessionId, String userMessage, String assistantReply) {
        List<String> history = new ArrayList<>(memory.getOrDefault(sessionId, new ArrayList<>()));

        history.add("User: " + userMessage);
        history.add("Assistant: " + assistantReply);

        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(history.size() - MAX_HISTORY_SIZE, history.size());
        }

        memory.put(sessionId, history);
    }
}