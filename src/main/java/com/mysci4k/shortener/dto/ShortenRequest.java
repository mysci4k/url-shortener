package com.mysci4k.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ShortenRequest(
        @NotBlank(message = "URL cannot be blank")
        @Pattern(
                regexp = "^https?://.+",
                message = "URL must be a valid HTTP or HTTPS URL"
        )
        String url
) {}
