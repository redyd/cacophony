package com.mapatiri.node.model;

import java.util.HashSet;
import java.util.Set;

public class User {

    private final String login;
    private final String bcryptHash;
    private final int bcryptRound;
    private final String bcryptSalt;
    private Set<String> subscribers;
    private Set<String> userTags;

    public User(String login, String bcryptHash, int bcryptRound, String bcryptSalt) {
        this.login = login;
        this.bcryptHash = bcryptHash;
        this.bcryptRound = bcryptRound;
        this.bcryptSalt = bcryptSalt;

        this.subscribers = new HashSet<>();
        this.userTags = new HashSet<>();
    }

    public User() {
        this.login = "";
        this.bcryptHash = "";
        this.bcryptRound = 0;
        this.bcryptSalt = "";
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

    public String getBcryptSalt() {
        return bcryptSalt;
    }

    public Set<String> getSubscribers() {
        return subscribers;
    }

    public Set<String> getUserTags() {
        return userTags;
    }
}
