package com.example.urlshortener.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "url")
public class Url {
    @Id
    private String id;

    @Indexed(unique = true)
    private String shortUrl;

    private String longUrl;

    private Long createdAt;
}
