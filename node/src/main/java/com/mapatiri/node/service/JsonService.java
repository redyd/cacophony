package com.mapatiri.node.service;

import com.mapatiri.node.exception.NodeConfigSaveException;
import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.model.BcryptParams;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class JsonService implements ConfigService {

    private final Path path;
    private NodeConfig nodeConfig;

    public JsonService(String filename) throws NodeFileException {
        path = getPath(filename);
        nodeConfig = loadNodeConfig();
    }

    @Override
    public NodeConfig loadNodeConfig() throws NodeFileException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try {
            NodeConfig config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), NodeConfig.class);
            if (config == null) {
                throw new NodeFileException("Config vide pour: " + path);
            }

            return config;
        } catch (IOException e) {
            throw new NodeFileException(e.getMessage());
        }
    }

    @Override
    public void addUser(String username, int saltLenght, String bycrypt) throws NodeConfigSaveException {
        if (nodeConfig == null) {
            throw new IllegalStateException("NodeConfig pas chargé");
        }

        synchronized (this) {
            Map<String, User> users = this.nodeConfig.getUsers();

            if (users.containsKey(username)) {
                throw new IllegalArgumentException("Utilisateur deja existant");
            }

            users.put(username, new User(saltLenght, bycrypt));
            saveConfig(nodeConfig);
        }
    }

    @Override
    public Optional<User> getUser(String username) {
        if (nodeConfig == null) {
            return Optional.empty();
        }
        Map<String, User> users = this.nodeConfig.getUsers();
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public void addSubscription(String username, String subscription) throws NodeConfigSaveException {
        if (nodeConfig == null) {
            throw new IllegalStateException("NodeConfig pas chargé");
        }
        synchronized (this) {
            nodeConfig.getSubscriptions()//ai
                    .computeIfAbsent(username, k -> new HashSet<>())
                    .add(subscription);
            saveConfig(nodeConfig);

        }
    }

    @Override
    public List<String> getSubscribers(String username) {
        if (nodeConfig == null) return List.of(); //ai
        Set<String> subs = nodeConfig.getSubscriptions().get(username);

        return subs != null ? List.copyOf(subs) : List.of();//ai
    }

    @Override
    public void saveConfig(NodeConfig nodeConfig) throws NodeConfigSaveException {
        try {
            //synchronized cest comme transaction car il se peut quon utilise les methode en meme temps
            synchronized (this) {
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create();
                Path tempFile = Files.createTempFile("nodeConfig", ".json");

                try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                    gson.toJson(nodeConfig, writer);
                }

                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new NodeConfigSaveException(e.getMessage());
        }

    }

    private Path getPath(String filename) {
        return Path.of("src", "main", "resources", filename);
    }
}
