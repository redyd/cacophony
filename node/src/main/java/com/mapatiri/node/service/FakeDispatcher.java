package com.mapatiri.node.service;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class FakeDispatcher implements Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(FakeDispatcher.class);

    @Override
    public ClientConnection startAuth(SSLSocket socket) throws AuthentificationFailed {
        try {
            return new ClientConnection(socket, "");
        } catch (IOException e) {
            throw new AuthentificationFailed("Authentification failed");
        }
    }

    @Override
    public void submit(String request, String sender) throws BadRequestException {
        logger.info("Received request '{}' from {}", request, sender);
    }
}
