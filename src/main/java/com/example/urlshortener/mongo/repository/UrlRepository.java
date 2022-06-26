package com.example.urlshortener.mongo.repository;

import com.example.urlshortener.mongo.entity.Url;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends MongoRepository<Url, String> {

    @Cacheable(value = "short_url_cache", key = "#shortUrl")
    Url findLongUrlByShortUrl(String shortUrl);
}
