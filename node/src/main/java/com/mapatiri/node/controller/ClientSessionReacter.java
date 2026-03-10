package com.mapatiri.node.controller;

import com.mapatiri.node.model.ClientConnection;

public interface ClientSessionReacter {
    void onUserLogout(ClientConnection user);
    void onUserLogin(ClientConnection user);
}
