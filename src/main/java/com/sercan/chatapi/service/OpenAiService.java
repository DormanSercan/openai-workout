package com.sercan.chatapi.service;

import com.sercan.chatapi.dto.ai.ChatRequest;
import com.sercan.chatapi.dto.ai.ChatResponse;
import com.sercan.chatapi.dto.ai.openai.OpenAiRequest;
import com.sercan.chatapi.dto.ai.openai.OpenAiResponse;
import com.sercan.chatapi.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class OpenAiService {

    private final RestClient restClient;
    private final String openAiBaseUrl;
    private final String openAiApiKey;
    private final String model;

    public OpenAiService(RestClient.Builder builder,
                         @Value("${openai.api.base-url}") String openAiBaseUrl,
                         @Value("${openai.api.key}") String openAiApiKey,
                         @Value("${openai.model}") String model) {
        this.restClient = builder.build();
        this.openAiBaseUrl = openAiBaseUrl;
        this.openAiApiKey = openAiApiKey;
        this.model = model;
    }

    public ChatResponse chat(ChatRequest request) {
        try {
            log.info("Calling OpenAI Responses API with model={}", model);

            OpenAiRequest openAiRequest = new OpenAiRequest(
                    model,
                    request.getMessage()
            );

            ResponseEntity<OpenAiResponse> responseEntity = restClient.post()
                    .uri(openAiBaseUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(openAiRequest)
                    .retrieve()
                    .toEntity(OpenAiResponse.class);

            HttpStatusCode statusCode = responseEntity.getStatusCode();

            if (!statusCode.is2xxSuccessful()) {
                log.warn("OpenAI returned non-success status: {}", statusCode.value());
                throw new ExternalApiException("OpenAI returned non-success status: " + statusCode.value());
            }

            OpenAiResponse body = responseEntity.getBody();

            if (body == null) {
                log.warn("OpenAI returned empty response body");
                throw new ExternalApiException("OpenAI returned empty response");
            }

            return mapToChatResponse(body);

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while calling OpenAI", e);
            throw new ExternalApiException("Failed to get response from OpenAI", e);
        }
    }

    private ChatResponse mapToChatResponse(OpenAiResponse openAiResponse) {
        if (openAiResponse.getOutput() == null || openAiResponse.getOutput().isEmpty()) {
            throw new ExternalApiException("OpenAI response output is empty");
        }

        OpenAiResponse.Output firstOutput = openAiResponse.getOutput().get(0);

        if (firstOutput.getContent() == null || firstOutput.getContent().isEmpty()) {
            throw new ExternalApiException("OpenAI response content is empty");
        }

        String text = firstOutput.getContent().get(0).getText();

        if (text == null || text.isBlank()) {
            throw new ExternalApiException("OpenAI response text is empty");
        }

        return new ChatResponse(text);
    }
}