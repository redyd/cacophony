package com.mapatiri.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.node.config.AppModule;
import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.model.HeartbeatClock;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.server.ClientNodeServer;
import com.mapatiri.node.service.SpeakerAnnouncer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws NodeFileException {
        Thread.currentThread().setName("Cacophony node");
        Injector injector = Guice.createInjector(new AppModule());

        // == CONFIG FILE ==
        var configRepo = injector.getInstance(ConfigRepository.class);
        var userRepo = injector.getInstance(UserRepo.class);
        var tagRepo = injector.getInstance(TagRepo.class);

        configRepo.loadConfig(resolveResource("node-config.json"));
        userRepo.loadUsers(resolveResource("data/user.json"));
        tagRepo.loadTags(resolveResource("data/tag.json"));

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
