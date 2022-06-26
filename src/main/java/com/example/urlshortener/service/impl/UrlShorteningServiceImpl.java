package com.example.urlshortener.service.impl;

import com.example.urlshortener.model.ShortenUrlRequest;
import com.example.urlshortener.model.ShortenedUrlResponse;
import com.example.urlshortener.mongo.entity.Url;
import com.example.urlshortener.mongo.repository.UrlRepository;
import com.example.urlshortener.properties.UrlShortener;
import com.example.urlshortener.service.UrlShorteningService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Slf4j
@Service
public class UrlShorteningServiceImpl implements UrlShorteningService {
    @Autowired
    private UrlShortener urlShortener;
    @Autowired
    private UrlRepository urlRepository;

    public ShortenedUrlResponse createShortUrl(ShortenUrlRequest shortenUrlRequest)
        throws Exception {
        ShortenedUrlResponse shortenedUrlResponse =
            ShortenedUrlResponse.builder().originalUrl(shortenUrlRequest.getOriginalUrl()).build();
        if (StringUtils.isNotBlank(shortenUrlRequest.getCustomShortUrl())) {
            createCustomShortUrl(shortenUrlRequest, shortenedUrlResponse);
        } else {
            createRandomShortUrl(shortenedUrlResponse, 0);
        }
        return shortenedUrlResponse;
    }

    private void createRandomShortUrl(ShortenedUrlResponse shortenedUrlResponse, int retries)
        throws Exception {
        if (retries == urlShortener.getDuplicateKeyRetrial()) {
            throw new Exception("Please try again after some time");
        }
        char[] charSet =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random rand = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < urlShortener.getUrlLengthLimit(); j++) {
            int n = rand.nextInt(charSet.length);
            stringBuilder.append(charSet[n]);
        }
        try {
            Url url = Url.builder().longUrl(shortenedUrlResponse.getOriginalUrl())
                .shortUrl(stringBuilder.toString()).createdAt(Instant.now().toEpochMilli()).build();
            urlRepository.save(url);
            shortenedUrlResponse.setShortenedUrl(url.getShortUrl());
        } catch (DuplicateKeyException ex) {
            log.debug("Retrying generating short URL for {}, currently generated: {}",
                shortenedUrlResponse.getOriginalUrl(), stringBuilder);
            createRandomShortUrl(shortenedUrlResponse, retries + 1);
        } catch (Exception e) {
            log.error("Exception occurred while generating short URL for {}",
                shortenedUrlResponse.getOriginalUrl());
            throw e;
        }
    }

    private void createCustomShortUrl(ShortenUrlRequest shortenUrlRequest,
        ShortenedUrlResponse shortenedUrlResponse) {
        try {
            Url url = Url.builder().longUrl(shortenedUrlResponse.getOriginalUrl())
                .shortUrl(shortenUrlRequest.getCustomShortUrl())
                .createdAt(Instant.now().toEpochMilli()).build();
            urlRepository.save(url);
            shortenedUrlResponse.setShortenedUrl(url.getShortUrl());
        } catch (DuplicateKeyException ex) {
            log.error("Custom short URL already exists {}", shortenUrlRequest.getCustomShortUrl());
            throw ex;
        } catch (Exception e) {
            log.error("Exception occurred while generating short URL for {} with custom name",
                shortenedUrlResponse.getOriginalUrl());
            throw e;
        }
    }

    public String retrieveOriginalUrl(String shortUrl) {
        Url url = null;
        try {
            url = urlRepository.findLongUrlByShortUrl(shortUrl);
        } catch (Exception e) {
            log.error("Error while retrieving original URL for {}", shortUrl);
        }
        return url == null ? "" : url.getLongUrl();
    }
}
