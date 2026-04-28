package com.sercan.chatapi.controller;

import com.sercan.chatapi.dto.EchoRequest;
import com.sercan.chatapi.dto.EchoResponse;
import com.sercan.chatapi.service.EchoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EchoController {

    private final EchoService echoService;

    public EchoController(EchoService echoService) {
        this.echoService = echoService;
    }

    @PostMapping("/echo")
    public EchoResponse echo(@Valid @RequestBody EchoRequest request) {
        return echoService.echo(request);
    }
}