package com.mysci4k.shortener.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ShortenRequest(
        @NotBlank(message = "URL cannot be blank")
        @Pattern(
                regexp = "^https?://.+",
                message = "URL must be a valid HTTP or HTTPS URL"
        )
        String url,

        @Positive(message = "Expired in days must be a positive integer")
        @Max(value = 365, message = "Expired in days must be less than or equal to 365 days")
        Integer expiredInDays
) {}
