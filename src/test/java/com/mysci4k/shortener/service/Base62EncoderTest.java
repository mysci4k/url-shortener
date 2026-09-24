package com.mysci4k.shortener.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base62EncoderTest {
    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    void encodesAndDecodesCorrectly() {
        long originalId = 125L;

        String encoded = encoder.encode(originalId);
        long decoded = encoder.decode(encoded);

        assertThat(decoded).isEqualTo(originalId);
    }

    @Test
    void encodesZeroAsFirstAlphabetCharacter() {
        long originalId = 0L;

        String encoded = encoder.encode(originalId);

        assertThat(encoded).isEqualTo("0");
    }

    @Test
    void differentIdsProduceDifferentCodes() {
        long originalId1 = 125L;
        long originalId2 = 126L;

        String encoded1 = encoder.encode(originalId1);
        String encoded2 = encoder.encode(originalId2);

        assertThat(encoded1).isNotEqualTo(encoded2);
    }

    @Test
    void encodesLargeIds() {
        long originalId = 999_999_999_999L;

        String encoded = encoder.encode(originalId);
        long decoded = encoder.decode(encoded);

        assertThat(decoded).isEqualTo(originalId);
    }
}
