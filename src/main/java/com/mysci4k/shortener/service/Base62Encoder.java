package com.mysci4k.shortener.service;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();
    private static final long MASK = (1L << 48) - 1;
    private static final long MULTIPLIER = 0x2545F4914F6CDD1DL;
    private static final long INVERSE = 0x1D96D81ECD35L;

    public String encode(long id) {
        long scrambled = (id * MULTIPLIER) & MASK;
        if (scrambled == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();

        long value = scrambled;
        while (value > 0) {
            int index = (int) (value % BASE);
            sb.append(ALPHABET.charAt(index));

            value /= BASE;
        }

        return sb.reverse().toString();
    }

    public long decode(String code) {
        long scrambled = 0;
        for (char c : code.toCharArray()) {
            scrambled = scrambled * BASE + ALPHABET.indexOf(c);
        }

        return (scrambled * INVERSE) & MASK;
    }
}
