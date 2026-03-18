package com.mapatiri.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.node.config.AppModule;
import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.server.ClientNodeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws NodeFileException {
        Injector injector = Guice.createInjector(new AppModule());

        // == CONFIG FILE ==
        var configRepo = injector.getInstance(ConfigRepository.class);
        var userRepo = injector.getInstance(UserRepo.class);
        var tagRepo = injector.getInstance(TagRepo.class);

        logger.debug("args are {}", Arrays.toString(args));

        configRepo.loadConfig(Path.of("src", "main", "resources", "node-config.json"));
        userRepo.loadUsers(Path.of("src", "main", "resources", "data", "user.json"));
        tagRepo.loadTags(Path.of("src", "main", "resources", "data", "tag.json"));

        // == INIT OBJECTS ==
        var clientServer = new Thread(injector.getInstance(ClientNodeServer.class), "Node server");

        // == START ==
        clientServer.start();

        logger.info("========== Node started ==========");
    }

}
