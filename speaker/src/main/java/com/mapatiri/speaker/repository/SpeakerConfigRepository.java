package com.mapatiri.speaker.repository;

import com.mapatiri.speaker.model.SpeakerConfig;

import java.nio.file.Path;

public interface SpeakerConfigRepository {

    void loadConfig(Path path);

    SpeakerConfig getConfig();
}
