package com.mapatiri.speaker.repository;

import com.mapatiri.speaker.Exception.SpeakerFileException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonSpeakerRepositoryTest {

    @Test
    void testLoadConfig() {
        var repo = new JsonSpeakerRepository();
        try {
            repo.loadConfig(Path.of("src", "main", "resources", "speaker-config.json"));
            var conf = repo.getConfig();

            assertNotNull(conf);
            assertNotNull(conf.getMulticastAddress());
            assertNotEquals(0, conf.getMulticastPort());
            assertNotNull(conf.getNetworkInterface());
            assertNotNull(conf.getConfiguredDomains());
        } catch (SpeakerFileException e) {
            fail();
        }
    }
}