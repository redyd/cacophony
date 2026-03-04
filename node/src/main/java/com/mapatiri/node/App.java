package com.mapatiri.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.node.config.AppModule;
import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.server.TLSServer;

import java.nio.file.Path;

public class App {

    public static void main(String[] args) throws NodeFileException {
        Injector injector = Guice.createInjector(new AppModule());
        var server = injector.getInstance(TLSServer.class);
        var config = injector.getInstance(ConfigRepository.class);

        config.loadConfig(Path.of("node", "src", "main", "resources", "node-config.json"));

        server.run();
    }

}
