package com.mysci4k.shortener.repository;

import com.mysci4k.shortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    @Query("SELECT s FROM ShortUrl s WHERE s.shortCode = :shortCode AND s.deletedAt IS NULL")
    Optional<ShortUrl> findActiveByShortCode(@Param("shortCode") String shortCode);

    @Query(value = "SELECT nextval('short_url_id_seq')", nativeQuery = true)
    Long fetchNextId();

    @Modifying
    @Query("DELETE FROM ShortUrl s WHERE s.deletedAt IS NOT NULL AND s.deletedAt < :cutoff")
    int purgeSoftDeletedBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM ShortUrl s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :cutoff")
    int purgeExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}
