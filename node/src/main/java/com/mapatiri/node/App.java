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

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws NodeFileException, SocketCreation {
        Thread.currentThread().setName("Cacophony node");
        Injector injector = Guice.createInjector(new AppModule());

        // == CONFIG FILE ==
        var configRepo = (JsonConfigRepository) injector.getInstance(ConfigRepository.class);
        var tagRepo = (JsonTagRepository) injector.getInstance(TagRepo.class);
        var userRepo = (JsonUserRepository) injector.getInstance(UserRepo.class);
        var fileConfig = new FileConfiguration();

        Path configPath = fileConfig.resolveConfigPath(args);
        Path usersPath = fileConfig.resolveUserPath(args);
        Path tagsPath = fileConfig.resolveTagPath(args);

        LOGGER.info("Using files: config={}, users={}, tags={}", configPath, usersPath, tagsPath);

        configRepo.loadConfig(configPath);
        userRepo.loadUsers(usersPath);
        tagRepo.loadTags(tagsPath);

        var announcer = injector.getInstance(SpeakerAnnouncer.class);
        announcer.init(injector.getInstance(UDPFactory.class));

        // == INIT OBJECTS ==
        var clockThread = new Thread(injector.getInstance(HeartbeatClock.class), "Clock");
        var serverThread = new Thread(injector.getInstance(ClientNodeServer.class), "Client server");

        // == START ==
        clockThread.start();
        serverThread.start();

        LOGGER.info("========== Node started ==========");
    }

}
