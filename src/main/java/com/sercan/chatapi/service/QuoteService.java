package com.sercan.chatapi.service;

import com.sercan.chatapi.dto.QuoteResponse;
import com.sercan.chatapi.dto.external.ExternalQuoteResponse;
import com.sercan.chatapi.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class QuoteService {

    private final RestClient restClient;
    private final String quoteUrl;

    public QuoteService(RestClient.Builder builder,
                        @Value("${external.api.quote-url}") String quoteUrl) {
        this.restClient = builder.build();
        this.quoteUrl = quoteUrl;
    }

    public QuoteResponse getRandomQuote() {
        try {
            log.info("Calling external API: {}", quoteUrl);

            ResponseEntity<ExternalQuoteResponse> responseEntity = restClient.get()
                    .uri(quoteUrl)
                    .retrieve()
                    .toEntity(ExternalQuoteResponse.class);

            HttpStatusCode statusCode = responseEntity.getStatusCode();

            if (!statusCode.is2xxSuccessful()) {
                log.warn("External API returned non-success status: {}", statusCode.value());
                throw new ExternalApiException("External API returned non-success status: " + statusCode.value());
            }

            ExternalQuoteResponse body = responseEntity.getBody();

            if (body == null) {
                throw new ExternalApiException("External API returned empty response");
            }

            return mapToQuoteResponse(body);

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while calling external API", e);
            throw new ExternalApiException("Failed to fetch quote from external API", e);
        }
    }

    private QuoteResponse mapToQuoteResponse(ExternalQuoteResponse external) {
        return new QuoteResponse(
                external.getQuote(),
                external.getAuthor()
        );
    }
}