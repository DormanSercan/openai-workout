package com.sercan.chatapi.controller;

import com.sercan.chatapi.dto.QuoteResponse;
import com.sercan.chatapi.service.QuoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/quote")
    public QuoteResponse getQuote() {
        return quoteService.getRandomQuote();
    }
}