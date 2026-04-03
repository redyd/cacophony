package com.mapatiri.speaker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.speaker.config.AppModule;
import com.mapatiri.speaker.repository.JsonSpeakerRepository;
import com.mapatiri.speaker.repository.SpeakerConfigRepository;
import com.mapatiri.speaker.service.ConnectionCatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new AppModule());
        logger.info("========== Speaker started ==========");

        var repo = injector.getInstance(SpeakerConfigRepository.class);
        repo.loadConfig(resolveResource("speaker-config.json"));

        var connectionCatcher = injector.getInstance(ConnectionCatcher.class);
        var catcher = new Thread(connectionCatcher::listenUdp, "thread-multicast");

        catcher.start();
    }

    private static Path resolveResource(String name) {
        return Path.of("src", "main", "resources", name);
    }

}
