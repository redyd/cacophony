package com.mapatiri.node.service;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;

import javax.net.ssl.SSLSocket;
import java.net.Socket;

public interface Dispatcher {
    ClientConnection startAuth(Socket socket) throws AuthentificationFailed;
    void submit(String request, String sender) throws BadRequestException;
}
