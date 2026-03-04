package com.mapatiri.node.repository;

import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.model.NodeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonConfigRepository implements ConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonConfigRepository.class);

    private NodeConfig currentConfig;

    @Override
    public void loadConfig(Path path) throws NodeFileException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try {
            NodeConfig config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), NodeConfig.class);
            if (config == null) {
                throw new NodeFileException("Config vide pour: " + path);
            }

            this.currentConfig = config;
        } catch (IOException e) {
            throw new NodeFileException(e.getMessage());
        }
    }

    @Override
    public NodeConfig getConfig() {
        return currentConfig;
    }
}
