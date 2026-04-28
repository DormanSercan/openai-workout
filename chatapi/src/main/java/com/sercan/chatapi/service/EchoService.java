package com.sercan.chatapi.service;

import com.sercan.chatapi.dto.EchoRequest;
import com.sercan.chatapi.dto.EchoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EchoService {

    public EchoResponse echo(EchoRequest request) {
        log.info("Echo request received. messageLength={}", request.getMessage().length());
        return new EchoResponse(request.getMessage());
    }
}