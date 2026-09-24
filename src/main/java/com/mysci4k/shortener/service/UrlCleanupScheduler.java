package com.mysci4k.shortener.service;

import com.mysci4k.shortener.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlCleanupScheduler {
    private static final int RETENTION_DAYS = 30;

    private final ShortUrlRepository repository;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void purgeOldUrls() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        int deletedSoftDeletedUrls = repository.purgeSoftDeletedBefore(cutoffDate);
        int deletedExpiredUrls = repository.purgeExpiredBefore(cutoffDate);

        log.info("Cleanup job: deleted {} soft-deleted and {} expired URLs older than {} days",
                deletedSoftDeletedUrls, deletedExpiredUrls, RETENTION_DAYS);
    }
}
