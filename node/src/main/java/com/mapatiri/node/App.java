package com.mapatiri.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.node.config.AppModule;
import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.factory.UDPFactory;
import com.mapatiri.node.model.HeartbeatClock;
import com.mapatiri.node.repository.*;
import com.mapatiri.node.server.ClientNodeServer;
import com.mapatiri.node.service.SpeakerAnnouncer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws NodeFileException, SocketCreation {
        if (args.length < 3) {
            throw new IllegalArgumentException("Config file argument required");
        }

        Path configPath = Path.of(args[0]);
        Path usersPath = Path.of(args[1]);
        Path tagsPath = Path.of(args[2]);

        Thread.currentThread().setName("Cacophony node");
        Injector injector = Guice.createInjector(new AppModule());

        // == CONFIG FILE ==
        var configRepo = (JsonConfigRepository) injector.getInstance(ConfigRepository.class);
        var tagRepo = (JsonTagRepository) injector.getInstance(TagRepo.class);
        var userRepo = injector.getInstance(UserRepo.class);

        configRepo.loadConfig(configPath);
        userRepo.loadUsers(usersPath);
        tagRepo.loadTags(tagsPath);

        var announcer = injector.getInstance(SpeakerAnnouncer.class);
        announcer.init(injector.getInstance(UDPFactory.class));

        var clock = injector.getInstance(HeartbeatClock.class);
        clock.add(injector.getInstance(SpeakerAnnouncer.class));

        // == INIT OBJECTS ==
        var clockThread = new Thread(clock, "Clock");
        var serverThread = new Thread(injector.getInstance(ClientNodeServer.class), "Client server");

        // == START ==
        clockThread.start();
        serverThread.start();

        logger.info("========== Node started ==========");
    }

    private static Path resolveResource(String name) {
        return Path.of("src", "main", "resources", name);
    }

}
