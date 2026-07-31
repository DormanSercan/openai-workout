package com.sercan.chatapi.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
class AiChatIntegrationTest {

    private static final String AI_REPLY = "integration-ai-reply";

    private static final HttpServer OPEN_AI_TEST_SERVER =
            startOpenAiTestServer();

    private static final AtomicReference<String> RECEIVED_METHOD =
            new AtomicReference<>();

    private static final AtomicReference<String> RECEIVED_PATH =
            new AtomicReference<>();

    private static final AtomicReference<String> RECEIVED_AUTHORIZATION =
            new AtomicReference<>();

    private static final AtomicReference<String> RECEIVED_BODY =
            new AtomicReference<>();

    private static final AtomicBoolean FAIL_FIRST_REQUEST =
            new AtomicBoolean(false);

    private static final AtomicInteger REQUEST_COUNT =
            new AtomicInteger(0);

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void overrideOpenAiProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "openai.api.base-url",
                () -> "http://localhost:"
                        + OPEN_AI_TEST_SERVER.getAddress().getPort()
                        + "/v1/responses"
        );

        registry.add(
                "openai.api.key",
                () -> "integration-test-api-key"
        );

        registry.add(
                "retry.openai.max-attempts",
                () -> "3"
        );

        registry.add(
                "retry.openai.delay-ms",
                () -> "0"
        );
    }

    @BeforeEach
    void resetCapturedRequest() {
        RECEIVED_METHOD.set(null);
        RECEIVED_PATH.set(null);
        RECEIVED_AUTHORIZATION.set(null);
        RECEIVED_BODY.set(null);

        FAIL_FIRST_REQUEST.set(false);
        REQUEST_COUNT.set(0);
    }

    @Test
    void chat_shouldReturnAiReply_whenRequestIsValid() throws Exception {
        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "sessionId": "integration-session-1",
                                      "message": "Explain dependency injection."
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.reply").value(AI_REPLY));

        assertEquals(
                "POST",
                RECEIVED_METHOD.get()
        );

        assertEquals(
                "/v1/responses",
                RECEIVED_PATH.get()
        );

        assertEquals(
                "Bearer integration-test-api-key",
                RECEIVED_AUTHORIZATION.get()
        );

        assertTrue(
                RECEIVED_BODY.get()
                        .contains("Explain dependency injection.")
        );
    }

    @Test
    void chat_shouldRetryAndReturnAiReply_whenFirstOpenAiCallFails()
            throws Exception {

        // Arrange
        FAIL_FIRST_REQUEST.set(true);

        // Act & Assert
        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "sessionId": "retry-integration-session",
                                      "message": "Explain retry briefly."
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(
                        jsonPath("$.reply").value(AI_REPLY)
                );

        assertEquals(
                2,
                REQUEST_COUNT.get()
        );

        assertEquals(
                "POST",
                RECEIVED_METHOD.get()
        );

        assertEquals(
                "/v1/responses",
                RECEIVED_PATH.get()
        );

        assertEquals(
                "Bearer integration-test-api-key",
                RECEIVED_AUTHORIZATION.get()
        );

        assertTrue(
                RECEIVED_BODY.get()
                        .contains("Explain retry briefly.")
        );
    }

    @AfterAll
    static void stopOpenAiTestServer() {
        OPEN_AI_TEST_SERVER.stop(0);
    }

    private static HttpServer startOpenAiTestServer() {
        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(0),
                    0
            );

            server.createContext(
                    "/v1/responses",
                    AiChatIntegrationTest::handleOpenAiRequest
            );

            server.start();

            return server;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not start OpenAI test server",
                    exception
            );
        }
    }

    private static void handleOpenAiRequest(
            HttpExchange exchange
    ) throws IOException {

        int currentRequestNumber =
                REQUEST_COUNT.incrementAndGet();

        RECEIVED_METHOD.set(exchange.getRequestMethod());

        RECEIVED_PATH.set(
                exchange.getRequestURI().getPath()
        );

        RECEIVED_AUTHORIZATION.set(
                exchange.getRequestHeaders()
                        .getFirst("Authorization")
        );

        String requestBody = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        RECEIVED_BODY.set(requestBody);

        if (FAIL_FIRST_REQUEST.get()
                && currentRequestNumber == 1) {

            exchange.close();
            return;
        }

        String responseJson = """
            {
              "output": [
                {
                  "content": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ]
            }
            """.formatted(AI_REPLY);

        byte[] responseBytes =
                responseJson.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                MediaType.APPLICATION_JSON_VALUE
        );

        exchange.sendResponseHeaders(
                200,
                responseBytes.length
        );

        try (OutputStream responseBody =
                     exchange.getResponseBody()) {

            responseBody.write(responseBytes);
        }
    }
}