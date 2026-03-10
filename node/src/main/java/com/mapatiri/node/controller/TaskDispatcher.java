package com.mapatiri.node.controller;

import com.google.inject.Inject;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.service.AuthTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class TaskDispatcher implements Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(TaskDispatcher.class);

    private final AuthTaskManager auth;

    @Inject
    public TaskDispatcher(AuthTaskManager auth) {
        this.auth = auth;
    }

    @Override
    public ClientConnection startAuth(Socket socket) throws AuthentificationFailed {
        return auth.startHandshake(socket);
    }

    @Override
    public void submit(String request, String sender) throws BadRequestException {
        logger.info("Received message from {}: {}", sender, request);
    }
}
