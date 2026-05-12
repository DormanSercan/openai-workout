package com.sercan.chatapi.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "retry.openai")
public class RetryProperties {

    @Min(1)
    private int maxAttempts;

    @Min(0)
    private long delayMs;
}