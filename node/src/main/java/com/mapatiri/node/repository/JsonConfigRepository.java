package com.mapatiri.node.repository;

import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.model.NodeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implémentation du {@link ConfigRepository} pour du json.
 *
 * @author Timëo
 */
public class JsonConfigRepository implements ConfigRepository {

    private NodeConfig currentConfig;

    public void loadConfig(Path path) throws NodeFileException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try {
            NodeConfig config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), NodeConfig.class);
            if (config == null) {
                throw new NodeFileException("Configuration file is empty for: " + path);
            }

            this.currentConfig = config;
        } catch (IOException e) {
            throw new NodeFileException(e.getMessage());
        }
    }

    @Override
    public NodeConfig getConfig() {
        if (currentConfig == null) {
            throw new IllegalStateException("You should init the configuration before requesting it");
        }
        return currentConfig;
    }

    @Override
    public String buildAtDomain(String element) {
        if (currentConfig == null) {
            throw new IllegalStateException("You should init the configuration before requesting it");
        }
        return element + "@" + currentConfig.getCurrentDomain();
    }
}
