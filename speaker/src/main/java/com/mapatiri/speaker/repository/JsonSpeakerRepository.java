package com.mapatiri.speaker.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapatiri.speaker.exception.SpeakerFileException;
import com.mapatiri.speaker.model.SpeakerConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonSpeakerRepository implements SpeakerConfigRepository {

    private SpeakerConfig currentConfig;

    @Override
    public void loadConfig(Path path) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try {
            SpeakerConfig config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), SpeakerConfig.class);
            if (config == null) {
                throw new SpeakerFileException("Config vide pour: " + path);
            }

            this.currentConfig = config;
        } catch (IOException e) {
            throw new SpeakerFileException(e.getMessage());
        }
    }

    public SpeakerConfig getConfig() {
        return currentConfig;
    }
}
