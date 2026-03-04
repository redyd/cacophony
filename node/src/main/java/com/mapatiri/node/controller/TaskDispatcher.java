package com.mapatiri.node.controller;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.service.AuthTaskManager;
import jakarta.inject.Inject;

import java.net.Socket;
import java.util.logging.Logger;

public class TaskDispatcher implements Dispatcher {

    private static final Logger logger = Logger.getLogger(TaskDispatcher.class.getName());

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

    }
}
