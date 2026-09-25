package com.mysci4k.shortener.controller;

import com.mysci4k.shortener.ratelimit.RateLimited;
import com.mysci4k.shortener.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final UrlShortenerService service;

    @GetMapping("/{shortCode}")
    @RateLimited(capacity = 50, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {
        String originalUrl = service.getOriginalUrlAndTrack(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }
}
