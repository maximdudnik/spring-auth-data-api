package com.example.authapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.data-api")
public record DataApiProperties(String url, String internalToken) {
}
