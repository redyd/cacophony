package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.task.AuthTask;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;

import java.io.*;
import java.net.Socket;

public class AuthTaskManager {

    private final LoginTask loginTask;
    private final RegisterTask registerTask;

    @Inject
    public AuthTaskManager(LoginTask loginTask, RegisterTask registerTask) {
        this.loginTask = loginTask;
        this.registerTask = registerTask;
    }

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

            String response = in.readLine();

            // TODO check message validity
            AuthTask task;

            if (response.startsWith("LOGIN")) {
                task = loginTask;
            } else if (response.startsWith("ONBOARDING")) {
                task = registerTask;
            } else {
                // TODO send error message
                out.println("ERROR");
                throw new AuthentificationFailed("Invalid response");
            }

            return task.authenticate(client, in, out);
        } catch (IOException e) {
            throw new AuthentificationFailed("Error getting connection to client");
        }
    }
}
