package com.mysci4k.shortener.dto;

import java.time.LocalDateTime;

public record ShortenResponse(
        String shortUrl,
        String originalUrl,
        String shortCode,
        LocalDateTime expiresAt
) {}
