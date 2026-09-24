package com.mysci4k.shortener.controller;

import com.mysci4k.shortener.dto.ShortenRequest;
import com.mysci4k.shortener.dto.ShortenResponse;
import com.mysci4k.shortener.dto.UrlStatsResponse;
import com.mysci4k.shortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {
    private final UrlShortenerService service;

    @PostMapping
    public ResponseEntity<ShortenResponse> generateShortenedLink(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = service.createShortenedUrl(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<UrlStatsResponse> retrieveUrlStats(@PathVariable String shortCode) {
        UrlStatsResponse response = service.getUrlStats(shortCode);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        service.deleteUrl(shortCode);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
