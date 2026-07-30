package com.sercan.chatapi.controller;

import com.sercan.chatapi.controller.EchoController;
import com.sercan.chatapi.dto.EchoResponse;
import com.sercan.chatapi.service.EchoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EchoController.class)
class EchoControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EchoService echoService;

    @Test
    void echo_shouldReturnOk_whenRequestIsValid() throws Exception {
        // Arrange
        EchoResponse serviceResponse = new EchoResponse("Hello");

        when(echoService.echo(any()))
                .thenReturn(serviceResponse);

        // Act & Assert
        mockMvc.perform(
                        post("/api/echo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "message": "Hello"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.message").value("Hello"));

        verify(echoService).echo(any());
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

        verifyNoInteractions(echoService);
    }
}