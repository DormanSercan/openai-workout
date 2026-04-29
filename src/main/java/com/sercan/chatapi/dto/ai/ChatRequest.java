package com.sercan.chatapi.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Session ID cannot be empty")
    private String sessionId;

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message cannot be longer than 1000 characters")
    private String message;
}