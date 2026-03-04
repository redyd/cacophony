package com.mapatiri.node.model;

import java.util.HashSet;
import java.util.Set;

public class User {

    private final String login;
    private final String bcryptHash;
    private final int bcryptRound;
    private final Set<String> subscribers;
    private final Set<String> userTags;

    public User(String login, String bcryptHash, int bcryptRound) {
        this.login = login;
        this.bcryptHash = bcryptHash;
        this.bcryptRound = bcryptRound;

        this.subscribers = new HashSet<>();
        this.userTags = new HashSet<>();
    }

    public String getLogin() {
        return login;
    }

    public String getBcryptHash() {
        return bcryptHash;
    }

    public int getBcryptRound() {
        return bcryptRound;
    }

    public Set<String> getSubscribers() {
        return subscribers;
    }

    public Set<String> getUserTags() {
        return userTags;
    }
}
