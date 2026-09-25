package com.mysci4k.shortener.controller;

import com.mysci4k.shortener.dto.ShortenRequest;
import com.mysci4k.shortener.dto.ShortenResponse;
import com.mysci4k.shortener.dto.UrlStatsResponse;
import com.mysci4k.shortener.ratelimit.RateLimited;
import com.mysci4k.shortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {
    private final UrlShortenerService service;

    @PostMapping
    @RateLimited(capacity = 10, duration = 1, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<ShortenResponse> generateShortenedLink(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = service.createShortenedUrl(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}/stats")
    @RateLimited(capacity = 30, duration = 1, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<UrlStatsResponse> retrieveUrlStats(@PathVariable String shortCode) {
        UrlStatsResponse response = service.getUrlStats(shortCode);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{shortCode}")
    @RateLimited(capacity = 10, duration = 1, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        service.deleteUrl(shortCode);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
