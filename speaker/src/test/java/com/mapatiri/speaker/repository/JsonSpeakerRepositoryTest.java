package com.mapatiri.speaker.repository;

import com.mapatiri.speaker.exception.SpeakerFileException;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonSpeakerRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void loadConfigLoadsValidJsonFile() {
        var repo = new JsonSpeakerRepository();

        assertDoesNotThrow(() -> repo.loadConfig(Path.of("src", "main", "resources", "speaker-config.json")));

        var conf = repo.getConfig();
        assertNotNull(conf);
        assertNotNull(conf.getMulticastAddress());
        assertNotEquals(0, conf.getMulticastPort());
        assertNotNull(conf.getNetworkInterface());
        assertNotNull(conf.getConfiguredDomains());
    }

    @Test
    void loadConfigThrowsForMissingFile() {
        var repo = new JsonSpeakerRepository();
        Path missing = tempDir.resolve("missing.json");

        assertThrows(SpeakerFileException.class, () -> repo.loadConfig(missing));
    }

    @Test
    void loadConfigThrowsForEmptyConfig() throws Exception {
        var repo = new JsonSpeakerRepository();
        Path file = tempDir.resolve("empty.json");
        Files.writeString(file, "null", StandardCharsets.UTF_8);

        SpeakerFileException exception = assertThrows(SpeakerFileException.class, () -> repo.loadConfig(file));
        assertTrue(exception.getMessage().contains("Config vide"));
    }
}
