package com.sercan.chatapi.dto.external;

import lombok.Data;

@Data
public class ExternalQuoteResponse {
    private int id;
    private String quote;
    private String author;
}