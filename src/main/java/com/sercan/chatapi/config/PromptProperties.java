package com.sercan.chatapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "chat.prompt")
public class PromptProperties {
    private String system;
    private String noHistoryMessage;
}