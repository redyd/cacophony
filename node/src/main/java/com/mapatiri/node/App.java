package com.mapatiri.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.node.config.AppModule;
import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.server.ClientNodeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws NodeFileException {
        Injector injector = Guice.createInjector(new AppModule());

        // == CONFIG FILE ==
        var config = injector.getInstance(ConfigRepository.class);
        config.loadConfig(Path.of("node", "src", "main", "resources", "node-config.json"));

        // == INIT OBJECTS ==
        var clientServer = new Thread(injector.getInstance(ClientNodeServer.class));

        // == START ==
        clientServer.start();

        logger.info("<== Node started ==>");
    }

}
