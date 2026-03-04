package com.mapatiri.node.task;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;

import java.net.Socket;

public interface AuthTask {
    ClientConnection authenticate(Socket client) throws AuthentificationFailed;
}
