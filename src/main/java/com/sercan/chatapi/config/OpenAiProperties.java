package com.sercan.chatapi.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    private Api api;

    @NotBlank
    private String model;

    @Data
    public static class Api {

        @NotBlank
        private String baseUrl;

        @NotBlank
        private String key;
    }
}