package com.mapatiri.node.service;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.task.AuthTask;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;

import java.io.*;
import java.net.Socket;

public class AuthTaskManager {

    public ClientConnection startHandshake(Socket client) throws AuthentificationFailed {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream())
                    ), true
            );

            // TODO send WELCOME
            out.println("WELCOME");

            // TODO wait for reply
            String response = in.readLine();

            // TODO check message validity
            AuthTask task;

            if (response.startsWith("LOGIN")) {
                task = new LoginTask();
            } else if (response.startsWith("ONBOARDING")) {
                task = new RegisterTask();
            } else {
                // TODO send error message
                out.println("ERROR");
                throw new AuthentificationFailed("Invalid response");
            }

            return task.authenticate(client);
        } catch (IOException e) {
            throw new AuthentificationFailed("Error getting connection to client");
        }
    }
}
