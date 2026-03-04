package com.mapatiri.node.task;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;

import java.net.Socket;

public class LoginTask implements AuthTask {
    @Override
    public ClientConnection authenticate(Socket client) throws AuthentificationFailed {
        return null;
    }
}
