package com.example.urlshortener.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("urlshortener")
public class UrlShortener {
    private int urlLengthLimit;
    private int duplicateKeyRetrial;
    private long cacheSize;
}
