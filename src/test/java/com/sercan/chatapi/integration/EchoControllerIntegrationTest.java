package com.sercan.chatapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EchoControllerIntegrationTest {

    private static final String MESSAGE = "integration-test-message";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void echo_shouldReturnSameMessage_whenRequestIsValid() throws Exception {
        mockMvc.perform(
                        post("/api/echo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "message": "%s"
                                        }
                                        """.formatted(MESSAGE))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }

    @Test
    void echo_shouldReturnBadRequest_whenMessageIsBlank() throws Exception {
        mockMvc.perform(
                        post("/api/echo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "message": ""
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());
    }
}