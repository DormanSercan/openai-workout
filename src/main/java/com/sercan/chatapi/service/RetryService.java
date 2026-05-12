package com.sercan.chatapi.service;

import com.sercan.chatapi.config.RetryProperties;
import com.sercan.chatapi.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.function.Supplier;

@Service
@Slf4j
public class RetryService {

    private final RetryProperties retryProperties;

    public RetryService(RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
    }

    public <T> T executeWithRetry(String operationName, Supplier<T> operation) {
        int attempt = 1;

        while (true) {
            try {
                return operation.get();

            } catch (ResourceAccessException | HttpServerErrorException.BadGateway |
                     HttpServerErrorException.ServiceUnavailable |
                     HttpServerErrorException.GatewayTimeout e) {

                if (attempt >= retryProperties.getMaxAttempts()) {
                    log.error("{} failed after {} attempts", operationName, attempt, e);
                    throw new ExternalApiException(operationName + " failed after retry attempts", e);
                }

                log.warn(
                        "{} failed. Retrying... attempt={}/{} delayMs={}",
                        operationName,
                        attempt,
                        retryProperties.getMaxAttempts(),
                        retryProperties.getDelayMs()
                );

                sleep();

                attempt++;
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(retryProperties.getDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException("Retry interrupted", e);
        }
    }
}