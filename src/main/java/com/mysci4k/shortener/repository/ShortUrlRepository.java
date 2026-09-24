package com.mysci4k.shortener.repository;

import com.mysci4k.shortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
}
