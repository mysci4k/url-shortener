package com.mysci4k.shortener.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String shortCode) {
        super("Url not found for short code: " + shortCode);
    }
}
