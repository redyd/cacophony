package com.mapatiri.speaker.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.speaker.repository.JsonSpeakerRepository;
import com.mapatiri.speaker.repository.SpeakerConfigRepository;
import com.mapatiri.speaker.service.ConnectionCatcher;
import com.mapatiri.speaker.service.SpeakerServer;


public class AppModule extends AbstractModule {

    @Override
    protected void configure() {

        // Repo
        bind(SpeakerConfigRepository.class).to(JsonSpeakerRepository.class).in(Singleton.class);

        // ConnectionCatcher
        bind(SpeakerServer.class).in(Singleton.class);
        bind(ConnectionCatcher.class).in(Singleton.class);
    }

}
