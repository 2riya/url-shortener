package com.example.urlshortener.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;

@Component
public class ValidationUtil {

    public static boolean validateString(String str, String errorMessage) {
        if (StringUtils.isBlank(str)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return true;
    }

    public static boolean validateURL(String str) {
        try {
            new URL(str).toURI();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL");
        }
        return true;
    }

    public static boolean validateCustomShortUrl(String str, int length) {
        if (StringUtils.isBlank(str)) {
            return true;
        } else if (str.length() > length) {
            throw new IllegalArgumentException("Custom name length exceeded");
        } else if (!str.matches("^[a-zA-Z0-9]*$")) {
            throw new IllegalArgumentException("Custom name contains invalid characters");
        }
        return true;
    }
}
