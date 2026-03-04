package com.mapatiri.node.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
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
}
