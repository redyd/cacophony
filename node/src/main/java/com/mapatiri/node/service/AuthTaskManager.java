package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.task.AuthTask;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Gestionnaire d'authentification du client.
 * Cette classe permet de rediriger vers une connexion ou une inscription, en fonction de ce que le client répond.
 * En cas de succès d'authentification, la méthode {@code startHandshake} renverra une connexion client
 * (et avec la connexion active dans le {@link ConnectionStore}.
 * En cas d'échec, {@code AuthentificationFailed} est levé.
 *
 * @author Timëo
 */
public class AuthTaskManager {

    private final LoginTask loginTask;
    private final RegisterTask registerTask;
    private final ConfigRepository configRepository;

    @Inject
    public AuthTaskManager(LoginTask loginTask, RegisterTask registerTask, ConfigRepository configRepository) {
        this.loginTask = loginTask;
        this.registerTask = registerTask;
        this.configRepository = configRepository;
    }

    public ClientConnection startHandshake(Socket client) throws AuthentificationFailed {
        try {
            NodeConfig config = this.configRepository.getConfig();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream())
                    ), true
            );

            // TODO send WELCOME
            out.println("WELCOME " + config.getCurrentDomain() + randomString(22));

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

    private String randomString(int length) {
        String digits = "0123456789";
        String letters = "abcdefghijklmnopqrstuvwxyz";
        String specials = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String all = digits + letters + letters.toUpperCase() + specials;

        Random r = new Random();
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = all.charAt(r.nextInt(all.length()));
        }
        return new String(chars);
    }
}
