package com.sercan.chatapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessage {
    private ChatRole role;
    private String content;
}