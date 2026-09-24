package com.mysci4k.shortener.repository;

import com.mysci4k.shortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);

    @Query(value = "SELECT nextval('short_url_id_seq')", nativeQuery = true)
    Long fetchNextId();
}
