package com.sercan.chatapi.controller;

import com.sercan.chatapi.dto.ai.ChatRequest;
import com.sercan.chatapi.dto.ai.ChatResponse;
import com.sercan.chatapi.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiChatController.class)
class AiChatControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpenAiService openAiService;

    @Test
    void chat_shouldReturnOk_whenRequestIsValid() throws Exception {
        // Arrange
        ChatResponse serviceResponse =
                new ChatResponse("Dependency injection provides dependencies from outside.");

        when(openAiService.chat(any(ChatRequest.class)))
                .thenReturn(serviceResponse);

        // Act & Assert
        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sessionId": "session-1",
                                          "message": "Explain dependency injection."
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.reply")
                        .value("Dependency injection provides dependencies from outside."));

        ArgumentCaptor<ChatRequest> requestCaptor =
                ArgumentCaptor.forClass(ChatRequest.class);

        verify(openAiService).chat(requestCaptor.capture());

        ChatRequest capturedRequest = requestCaptor.getValue();

        assertEquals("session-1", capturedRequest.getSessionId());
        assertEquals(
                "Explain dependency injection.",
                capturedRequest.getMessage()
        );
    }

    @Test
    void chat_shouldReturnBadRequest_whenMessageIsBlank() throws Exception {
        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "sessionId": "session-1",
                                      "message": ""
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(openAiService);
    }

    @Test
    void chat_shouldReturnBadRequest_whenSessionIdIsBlank() throws Exception {
        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "sessionId": "",
                                      "message": "Explain dependency injection."
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(openAiService);
    }

    @Test
    void chat_shouldReturnInternalServerError_whenOpenAiServiceThrowsUnexpectedException()
            throws Exception {

        // Arrange
        when(openAiService.chat(any(ChatRequest.class)))
                .thenThrow(new RuntimeException("Unexpected test error"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "sessionId": "session-1",
                                      "message": "Explain dependency injection."
                                    }
                                    """)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));

        verify(openAiService).chat(any(ChatRequest.class));
    }
}