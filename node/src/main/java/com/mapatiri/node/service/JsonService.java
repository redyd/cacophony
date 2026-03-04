package com.mapatiri.node.service;

import com.mapatiri.node.exception.NodeConfigSaveException;
import com.mapatiri.node.exception.NodeFileException;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonService implements ConfigService {

    private NodeConfig currentConfig;

    @Override
    public NodeConfig loadConfig(Path path) throws NodeFileException {
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
            return config;
        } catch (IOException e) {
            throw new NodeFileException(e.getMessage());
        }
    }

    @Override
    public User addUser(String login, String bcrypt, int saltLength) throws NodeConfigSaveException {
        if (currentConfig == null) {
            throw new IllegalStateException("NodeConfig pas chargé");
        }

        synchronized (this) {
            Map<String, User> users = this.currentConfig
                    .getUsers()
                    .stream()
                    .collect(Collectors.toMap(
                            User::getLogin,
                            Function.identity()));

            if (users.containsKey(login)) {
                throw new IllegalArgumentException("Utilisateur deja existant");
            }

            User user = new User(login, bcrypt, saltLength);

            users.put(login, user);
            saveConfig(currentConfig);

            return user;
        }
    }

    @Override
    public Optional<User> getUser(String login) {
        if (currentConfig == null) {
            return Optional.empty();
        }

        return this.currentConfig
                .getUsers()
                .stream()
                .filter(user -> user.getLogin().equals(login))
                .findFirst();
    }

    @Override
    public void addSubscription(User to, String subscriber) throws NodeConfigSaveException {
        if (currentConfig == null) {
            throw new IllegalStateException("NodeConfig pas chargé");
        }

        synchronized (this) {
            currentConfig.getSubscriptions()//ai
                    .computeIfAbsent(username, k -> new HashSet<>())
                    .add(subscription);
            saveConfig(currentConfig);

        }
    }

    @Override
    public List<String> getSubscribers(String username) {
        if (currentConfig == null) return List.of(); //ai
        Set<String> subs = currentConfig.getSubscriptions().get(username);

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
