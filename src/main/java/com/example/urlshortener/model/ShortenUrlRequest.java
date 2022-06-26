package com.example.urlshortener.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortenUrlRequest {
    private String originalUrl;
    private String customShortUrl;
}
