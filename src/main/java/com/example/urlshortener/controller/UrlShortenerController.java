package com.example.urlshortener.controller;

import com.example.urlshortener.model.ShortenUrlRequest;
import com.example.urlshortener.model.ShortenedUrlResponse;
import com.example.urlshortener.properties.UrlShortener;
import com.example.urlshortener.service.UrlShorteningService;
import com.example.urlshortener.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("/")
public class UrlShortenerController {
    @Autowired
    private UrlShortener urlShortener;
    @Autowired
    private UrlShorteningService urlShorteningService;

    @PostMapping(value = "/create")
    public ResponseEntity<ShortenedUrlResponse> createShortUrl(
        @RequestBody ShortenUrlRequest shortenUrlRequest) {
        try {
            ValidationUtil.validateString(shortenUrlRequest.getOriginalUrl(),
                "Original URL must be present");
            ValidationUtil.validateURL(shortenUrlRequest.getOriginalUrl());
            ValidationUtil.validateCustomShortUrl(shortenUrlRequest.getCustomShortUrl(),
                urlShortener.getUrlLengthLimit());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity()
                .body(ShortenedUrlResponse.builder().errorMessage(e.getMessage()).build());
        }
        ShortenedUrlResponse shortenedUrlResponse;
        try {
            shortenedUrlResponse = urlShorteningService.createShortUrl(shortenUrlRequest);
            return new ResponseEntity<>(shortenedUrlResponse, HttpStatus.CREATED);
        } catch (DuplicateKeyException duplicateKeyException) {
            shortenedUrlResponse =
                ShortenedUrlResponse.builder().originalUrl(shortenUrlRequest.getOriginalUrl())
                    .errorMessage("URL name not available").build();
            return ResponseEntity.unprocessableEntity().body(shortenedUrlResponse);
        } catch (Exception e) {
            shortenedUrlResponse =
                ShortenedUrlResponse.builder().originalUrl(shortenUrlRequest.getOriginalUrl())
                    .errorMessage(e.getMessage()).build();
            return ResponseEntity.internalServerError().body(shortenedUrlResponse);
        }
    }

    @GetMapping(value = "/{shortUrl}")
    public ResponseEntity<Void> redirectUrl(@PathVariable String shortUrl) {
        try {
            ValidationUtil.validateString(shortUrl, "Short URL must be present");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String originalUrl = urlShorteningService.retrieveOriginalUrl(shortUrl);
        if (StringUtils.isBlank(originalUrl)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
    }
}
