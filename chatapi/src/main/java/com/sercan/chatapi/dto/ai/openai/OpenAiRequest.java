package com.sercan.chatapi.dto.ai.openai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenAiRequest {
    private String model;
    private String input;
}