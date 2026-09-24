CREATE SEQUENCE short_url_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE short_urls (
    id BIGINT PRIMARY KEY DEFAULT nextval('short_url_id_seq'),
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_short_urls_short_code ON short_urls (short_code);