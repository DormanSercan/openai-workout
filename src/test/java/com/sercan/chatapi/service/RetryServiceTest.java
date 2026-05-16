package com.sercan.chatapi.service;

import com.sercan.chatapi.config.RetryProperties;
import com.sercan.chatapi.exception.ExternalApiException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryServiceTest {

    @Test
    void executeWithRetry_shouldReturnResult_whenOperationSucceedsFirstTry() {
        RetryProperties properties = new RetryProperties();
        properties.setMaxAttempts(3);
        properties.setDelayMs(0);

        RetryService retryService = new RetryService(properties);

        String result = retryService.executeWithRetry(
                "test operation",
                () -> "success"
        );

        assertEquals("success", result);
    }

    @Test
    void executeWithRetry_shouldRetryAndReturnResult_whenOperationSucceedsAfterFailure() {
        RetryProperties properties = new RetryProperties();
        properties.setMaxAttempts(3);
        properties.setDelayMs(0);

        RetryService retryService = new RetryService(properties);

        AtomicInteger counter = new AtomicInteger(0);

        String result = retryService.executeWithRetry(
                "test operation",
                () -> {
                    if (counter.incrementAndGet() < 2) {
                        throw new ResourceAccessException("Temporary network error");
                    }
                    return "success";
                }
        );

        assertEquals("success", result);
        assertEquals(2, counter.get());
    }

    @Test
    void executeWithRetry_shouldThrowException_whenMaxAttemptsExceeded() {
        RetryProperties properties = new RetryProperties();
        properties.setMaxAttempts(3);
        properties.setDelayMs(0);

        RetryService retryService = new RetryService(properties);

        AtomicInteger counter = new AtomicInteger(0);

        ExternalApiException exception = assertThrows(
                ExternalApiException.class,
                () -> retryService.executeWithRetry(
                        "test operation",
                        () -> {
                            counter.incrementAndGet();
                            throw new ResourceAccessException("Persistent network error");
                        }
                )
        );

        assertEquals(3, counter.get());
        assertTrue(exception.getMessage().contains("failed after retry attempts"));
    }
}