ALTER TABLE short_urls ADD COLUMN expires_at TIMESTAMP;

CREATE INDEX idx_short_urls_expires_at
    ON short_urls (expires_at)
    WHERE expires_at IS NOT NULL;