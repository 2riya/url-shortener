package com.example.urlshortener.service;

import com.example.urlshortener.model.ShortenUrlRequest;
import com.example.urlshortener.model.ShortenedUrlResponse;

public interface UrlShorteningService {
    ShortenedUrlResponse createShortUrl(ShortenUrlRequest shortenUrlRequest) throws Exception;
    String retrieveOriginalUrl(String shortUrl);
}
