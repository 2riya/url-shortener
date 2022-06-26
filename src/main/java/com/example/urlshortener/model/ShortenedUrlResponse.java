package com.example.urlshortener.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ShortenedUrlResponse {
    private String originalUrl;
    private String shortenedUrl;
    private String errorMessage;
}
