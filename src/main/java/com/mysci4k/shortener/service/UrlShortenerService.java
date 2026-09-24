package com.mysci4k.shortener.service;

import com.mysci4k.shortener.dto.ShortenRequest;
import com.mysci4k.shortener.dto.ShortenResponse;
import com.mysci4k.shortener.dto.UrlStatsResponse;
import com.mysci4k.shortener.entity.ShortUrl;
import com.mysci4k.shortener.exception.UrlNotFoundException;
import com.mysci4k.shortener.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {
    private final ShortUrlRepository repository;
    private final Base62Encoder base62Encoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortenResponse createShortenedUrl(ShortenRequest request) {
        Long id = repository.fetchNextId();
        String shortCode = base62Encoder.encode(id);

        ShortUrl entity = new ShortUrl(id, request.url(), shortCode);

        if (request.expiredInDays() != null) {
            entity.setExpiresAt(LocalDateTime.now().plusDays(request.expiredInDays()));
        }
        repository.save(entity);

        return new ShortenResponse(
                baseUrl + "/" + shortCode,
                entity.getOriginalUrl(),
                shortCode,
                entity.getExpiresAt()
        );
    }

    @Transactional
    public String getOriginalUrlAndTrack(String shortCode) {
        ShortUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        url.incrementClickCount();
        repository.save(url);

        return url.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String shortCode) {
        ShortUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return new UrlStatsResponse(
                url.getShortCode(),
                url.getOriginalUrl(),
                url.getClickCount(),
                url.getCreatedAt()
        );
    }
}
