package com.mysci4k.shortener.dto;

import java.time.LocalDateTime;

public record UrlStatsResponse(
        String shortCode,
        String originalUrl,
        long clickCount,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {}
