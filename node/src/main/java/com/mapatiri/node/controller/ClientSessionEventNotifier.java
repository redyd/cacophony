package com.mapatiri.node.controller;

import com.mapatiri.node.model.ClientConnection;

import java.util.HashSet;
import java.util.Set;

public class ClientSessionEventNotifier {
    private final Set<ClientSessionReacter> reacters;

    public ClientSessionEventNotifier() {
        this.reacters = new HashSet<>();
    }

    public void subscribe(ClientSessionReacter reacter) {
        reacters.add(reacter);
    }

    public void onUserLogout(ClientConnection client) {
        for (ClientSessionReacter reacter : reacters) {
            reacter.onUserLogout(client);
        }
    }

    public void onUserLogin(ClientConnection client) {
        for (ClientSessionReacter reacter : reacters) {
            reacter.onUserLogin(client);
        }
    }
}
