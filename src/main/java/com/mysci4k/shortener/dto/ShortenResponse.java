package com.mysci4k.shortener.dto;

public record ShortenResponse(
        String shortUrl,
        String originalUrl,
        String shortCode
) {}
