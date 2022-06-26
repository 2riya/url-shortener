package com.example.urlshortener.service.impl;

import com.example.urlshortener.model.ShortenUrlRequest;
import com.example.urlshortener.model.ShortenedUrlResponse;
import com.example.urlshortener.mongo.entity.Url;
import com.example.urlshortener.mongo.repository.UrlRepository;
import com.example.urlshortener.properties.UrlShortener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UrlShorteningServiceImplTest {
    @InjectMocks
    private UrlShorteningServiceImpl urlShorteningService;
    @Mock
    private UrlRepository urlRepository;
    @Mock
    private UrlShortener urlShortener;
    @Captor
    private ArgumentCaptor<Url> urlArgumentCaptor;

    // successfully created random url
    @Test
    void randomUrlCreationTest() throws Exception {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com/").build();
        Mockito.when(urlShortener.getUrlLengthLimit()).thenReturn(6);
        Mockito.when(urlShortener.getDuplicateKeyRetrial()).thenReturn(5);
        Mockito.when(urlRepository.save(Mockito.any()))
            .thenAnswer((Answer<Url>) invocation -> invocation.getArgument(0));

        ShortenedUrlResponse shortenedUrlResponse =
            urlShorteningService.createShortUrl(shortenUrlRequest);

        Mockito.verify(urlRepository, Mockito.times(1)).save(urlArgumentCaptor.capture());
        Assertions.assertNotNull(shortenedUrlResponse);
        Assertions.assertEquals(urlArgumentCaptor.getValue().getShortUrl(),
            shortenedUrlResponse.getShortenedUrl());
        Assertions.assertEquals(urlShortener.getUrlLengthLimit(), shortenedUrlResponse.getShortenedUrl().length());
    }

    // successfully created custom url
    @Test
    void customUrlCreationTest() throws Exception {
        ShortenUrlRequest shortenUrlRequest =
            ShortenUrlRequest.builder().originalUrl("https://www.google.com/").customShortUrl("xyzPQR").build();
        Mockito.when(urlRepository.save(Mockito.any()))
            .thenAnswer((Answer<Url>) invocation -> invocation.getArgument(0));

        ShortenedUrlResponse shortenedUrlResponse =
            urlShorteningService.createShortUrl(shortenUrlRequest);

        Mockito.verify(urlRepository, Mockito.times(1)).save(urlArgumentCaptor.capture());
        Assertions.assertNotNull(shortenedUrlResponse);
        Assertions.assertEquals(shortenUrlRequest.getCustomShortUrl(), shortenedUrlResponse.getShortenedUrl());
        Assertions.assertEquals(shortenUrlRequest.getCustomShortUrl(), urlArgumentCaptor.getValue().getShortUrl());
    }

    // duplicate key exception test

    // fetch original url
    @Test
    void fetchOriginalUrlTest() {
        Url url = Url.builder().shortUrl("xyzPQR").longUrl("https://www.google.com/").build();
        Mockito.when(urlRepository.findLongUrlByShortUrl(url.getShortUrl())).thenReturn(url);

        String originalUrl = urlShorteningService.retrieveOriginalUrl(url.getShortUrl());

        Mockito.verify(urlRepository, Mockito.times(1)).findLongUrlByShortUrl(url.getShortUrl());
        Assertions.assertNotNull(originalUrl);
        Assertions.assertEquals(url.getLongUrl(), originalUrl);
    }


    // original url not found
    @Test
    void originalUrlNotPresentTest() {
        String shortUrl = "xyzPQR";
        Mockito.when(urlRepository.findLongUrlByShortUrl(shortUrl)).thenReturn(null);

        String originalUrl = urlShorteningService.retrieveOriginalUrl(shortUrl);

        Mockito.verify(urlRepository, Mockito.times(1)).findLongUrlByShortUrl(shortUrl);
        Assertions.assertEquals("", originalUrl);
    }

}
