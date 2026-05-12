package com.sercan.chatapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final String clientName;

    public HttpLoggingInterceptor(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        long startTime = System.currentTimeMillis();

        try {
            ClientHttpResponse response = execution.execute(request, body);

            long duration = System.currentTimeMillis() - startTime;

            log.info(
                    "{} HTTP call completed: method={}, uri={}, status={}, durationMs={}",
                    clientName,
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode(),
                    duration
            );

            return response;

        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error(
                    "{} HTTP call failed: method={}, uri={}, durationMs={}",
                    clientName,
                    request.getMethod(),
                    request.getURI(),
                    duration,
                    e
            );

            throw e;
        }
    }
}