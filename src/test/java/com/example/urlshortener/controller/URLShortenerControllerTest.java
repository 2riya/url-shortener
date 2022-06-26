package com.example.urlshortener.controller;

import com.example.urlshortener.model.ShortenUrlRequest;
import com.example.urlshortener.model.ShortenedUrlResponse;
import com.example.urlshortener.properties.UrlShortener;
import com.example.urlshortener.service.UrlShorteningService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class URLShortenerControllerTest {
    @InjectMocks
    private UrlShortenerController urlShortenerController;
    @Mock
    private UrlShorteningService urlShorteningService;
    @Mock
    private UrlShortener urlShortener;

    @Test
    void blankOriginalUrlTest() {
        ShortenUrlRequest shortenUrlRequest = ShortenUrlRequest.builder().build();
        ResponseEntity<ShortenedUrlResponse> shortenedUrlResponse =
            urlShortenerController.createShortUrl(shortenUrlRequest);
        Assertions.assertNotNull(shortenedUrlResponse);
        Assertions.assertNotNull(shortenedUrlResponse.getBody());
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
            shortenedUrlResponse.getStatusCode());
        Assertions.assertEquals("Original URL must be present",
            shortenedUrlResponse.getBody().getErrorMessage());
    }

    @Test
    void invalidCharsCustomUrlTest() {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com")
                .customShortUrl("abc-*&").build();
        Mockito.when(urlShortener.getUrlLengthLimit()).thenReturn(6);
        ResponseEntity<ShortenedUrlResponse> shortenedUrlResponse =
            urlShortenerController.createShortUrl(shortenUrlRequest);
        Assertions.assertNotNull(shortenedUrlResponse);
        Assertions.assertNotNull(shortenedUrlResponse.getBody());
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
            shortenedUrlResponse.getStatusCode());
        Assertions.assertEquals("Custom name contains invalid characters",
            shortenedUrlResponse.getBody().getErrorMessage());
    }

    @Test
    void lengthExceededCustomUrlTest() {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com")
                .customShortUrl("abcdefghijk").build();
        Mockito.when(urlShortener.getUrlLengthLimit()).thenReturn(6);
        ResponseEntity<ShortenedUrlResponse> shortenedUrlResponse =
            urlShortenerController.createShortUrl(shortenUrlRequest);
        Assertions.assertNotNull(shortenedUrlResponse);
        Assertions.assertNotNull(shortenedUrlResponse.getBody());
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
            shortenedUrlResponse.getStatusCode());
        Assertions.assertEquals("Custom name length exceeded",
            shortenedUrlResponse.getBody().getErrorMessage());
    }

    @Test
    void validOriginalUrlTest() throws Exception {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com").build();
        ShortenedUrlResponse shortenedUrlResponse =
            ShortenedUrlResponse.builder().originalUrl(shortenUrlRequest.getOriginalUrl())
                .shortenedUrl("xyzPQR").build();
        Mockito.when(urlShorteningService.createShortUrl(shortenUrlRequest))
            .thenReturn(shortenedUrlResponse);
        ResponseEntity<ShortenedUrlResponse> shortenedUrlResponseEntity =
            urlShortenerController.createShortUrl(shortenUrlRequest);
        Assertions.assertNotNull(shortenedUrlResponseEntity);
        Assertions.assertNotNull(shortenedUrlResponseEntity.getBody());
        Assertions.assertEquals(HttpStatus.CREATED, shortenedUrlResponseEntity.getStatusCode());
        Assertions.assertEquals(shortenedUrlResponse.getShortenedUrl(),
            shortenedUrlResponseEntity.getBody().getShortenedUrl());
    }

    @Test
    void validCustomUrlTest() throws Exception {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com")
                .customShortUrl("xyzPQR").build();
        ShortenedUrlResponse shortenedUrlResponse =
            ShortenedUrlResponse.builder().originalUrl(shortenUrlRequest.getOriginalUrl())
                .shortenedUrl(shortenUrlRequest.getCustomShortUrl()).build();
        Mockito.when(urlShortener.getUrlLengthLimit()).thenReturn(6);
        Mockito.when(urlShorteningService.createShortUrl(shortenUrlRequest))
            .thenReturn(shortenedUrlResponse);
        ResponseEntity<ShortenedUrlResponse> shortenedUrlResponseEntity =
            urlShortenerController.createShortUrl(shortenUrlRequest);
        Assertions.assertNotNull(shortenedUrlResponseEntity);
        Assertions.assertNotNull(shortenedUrlResponseEntity.getBody());
        Assertions.assertEquals(HttpStatus.CREATED, shortenedUrlResponseEntity.getStatusCode());
        Assertions.assertEquals(shortenUrlRequest.getCustomShortUrl(),
            shortenedUrlResponseEntity.getBody().getShortenedUrl());
    }

    @Test
    void duplicateKeyExceptionTest() throws Exception {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com")
                .customShortUrl("xyzPQR").build();
        Mockito.when(urlShortener.getUrlLengthLimit()).thenReturn(6);
        Mockito.when(urlShorteningService.createShortUrl(Mockito.any()))
            .thenThrow(DuplicateKeyException.class);
        ResponseEntity<ShortenedUrlResponse> shortenedUrlResponseEntity =
            urlShortenerController.createShortUrl(shortenUrlRequest);
        Assertions.assertNotNull(shortenedUrlResponseEntity);
        Assertions.assertNotNull(shortenedUrlResponseEntity.getBody());
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
            shortenedUrlResponseEntity.getStatusCode());
        Assertions.assertEquals("URL name not available",
            shortenedUrlResponseEntity.getBody().getErrorMessage());
    }

    @Test
    void exceptionTest() throws Exception {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com")
                .customShortUrl("xyzPQR").build();
        Mockito.when(urlShortener.getUrlLengthLimit()).thenReturn(6);
        Mockito.when(urlShorteningService.createShortUrl(Mockito.any()))
            .thenThrow(new Exception("An exception occurred"));
        ResponseEntity<ShortenedUrlResponse> shortenedUrlResponseEntity =
            urlShortenerController.createShortUrl(shortenUrlRequest);
        Assertions.assertNotNull(shortenedUrlResponseEntity);
        Assertions.assertNotNull(shortenedUrlResponseEntity.getBody());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
            shortenedUrlResponseEntity.getStatusCode());
        Assertions.assertNotNull(shortenedUrlResponseEntity.getBody().getErrorMessage());
        Assertions.assertEquals("An exception occurred",
            shortenedUrlResponseEntity.getBody().getErrorMessage());
    }

    @Test
    void redirectionBadRequestTest() {
        String shortUrl = "";
        Mockito.when(urlShorteningService.retrieveOriginalUrl(shortUrl))
            .thenReturn("https://www.google.com/");
        ResponseEntity<Void> responseEntity = urlShortenerController.redirectUrl(shortUrl);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void redirectionSuccessTest() {
        String shortUrl = "xyzPQR";
        Mockito.when(urlShorteningService.retrieveOriginalUrl(shortUrl))
            .thenReturn("https://www.google.com/");
        ResponseEntity<Void> responseEntity = urlShortenerController.redirectUrl(shortUrl);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.FOUND, responseEntity.getStatusCode());
    }

    @Test
    void redirectionNotFoundTest() {
        String shortUrl = "xyzPQR";
        Mockito.when(urlShorteningService.retrieveOriginalUrl(shortUrl)).thenReturn("");
        ResponseEntity<Void> responseEntity = urlShortenerController.redirectUrl(shortUrl);
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }
}
