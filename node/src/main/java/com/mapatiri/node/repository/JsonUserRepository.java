package com.mapatiri.node.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapatiri.node.exception.UserConfigException;
import com.mapatiri.node.exception.UserConflictException;
import com.mapatiri.node.model.User;
import com.mapatiri.node.model.Users;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonUserRepository implements UserRepo {

    private final Gson gson;
    private Users usersConfig;
    private Path path;

    public JsonUserRepository() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    @Override
    public void loadUsers(Path path) throws UserConfigException {
        this.path = path;

        try {
            Users config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), Users.class);
            if (config == null) {
                throw new UserConfigException("Config vide pour: " + path);
            }

            this.usersConfig = config;
        } catch (IOException e) {
            throw new UserConfigException(e.getMessage());
        }
    }

    @Override
    public void addUser(User toAdd) throws IOException, UserConflictException {
        for (User curr : usersConfig.users()) {
            if (curr.getLogin().equals(toAdd.getLogin())) {
                throw new UserConflictException("This login already exists");
            }
        }

        usersConfig.users().add(toAdd);
        saveUsers();
    }

    @Override
    public User getUserFromLogin(String login) {
        for (User user : usersConfig.users()) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void addSubscriberToUser(String login, String subscriberId) throws IOException {
        User user = getUserFromLogin(login);
        if (user != null) {
            user.getSubscribers().add(subscriberId);
        }
        saveUsers();
    }

    @Override
    public void addUserTag(String login, String tag) throws IOException {
        User user = getUserFromLogin(login);
        user.getUserTags().add(tag);
        saveUsers();
    }

    private void saveUsers() throws IOException {
        if (usersConfig == null) {
            throw new IllegalStateException("Users repository must be initialized");
        }

        synchronized (this) {
            Files.writeString(path, gson.toJson(usersConfig), StandardCharsets.UTF_8);
        }
    }
}
