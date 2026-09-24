CREATE INDEX idx_short_urls_short_code_active
    ON short_urls (short_code)
    WHERE deleted_at IS NULL;