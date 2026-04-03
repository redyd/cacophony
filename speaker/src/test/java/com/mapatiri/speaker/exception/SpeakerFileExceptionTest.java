package com.mapatiri.speaker.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeakerFileExceptionTest {

    @Test
    void keepsProvidedMessage() {
        SpeakerFileException exception = new SpeakerFileException("config missing");

        assertEquals("config missing", exception.getMessage());
    }
}
