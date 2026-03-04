package com.mapatiri.node.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapatiri.node.exception.UserConfigException;
import com.mapatiri.node.model.User;
import com.mapatiri.node.model.Users;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class UserRepository implements UserRepo {

    Users usersConfig;

    @Override
    public void loadUsers(Path path) throws UserConfigException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

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
    public void addUser(String login, String bcryptHash, int bcryptRound, String bcryptSalt) {
        User user = new User(login, bcryptHash, bcryptRound, bcryptSalt);
        usersConfig.users().add(user);
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
    public void addSubscriberToUser(String login, String subscriberId) {
        User user = getUserFromLogin(login);
        if (user != null) {
            user.getSubscribers().add(subscriberId);
        }
    }

    @Override
    public void addUserTag(String login, String tag) {
        User user = getUserFromLogin(login);
        if (user != null) {
            user.getUserTags().add(tag);
        }
    }

    @Override
    public void saveUsers(Path path) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Files.writeString(path, gson.toJson(usersConfig), StandardCharsets.UTF_8);
    }

    @Override
    public Users getAllUsers() {
        return usersConfig;
    }
}
