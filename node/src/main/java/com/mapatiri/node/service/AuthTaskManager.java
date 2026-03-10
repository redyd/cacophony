package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.task.AuthTask;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(AuthTaskManager.class);
    private static final int RANDOM_STRING_LENGTH = 22;

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

            // send welcome with rand char
            String randomString = randomString();
            String welcome = new RegexGrammar().buildCommand("WELCOME", config.getCurrentDomain(), randomString);
            out.println(welcome);

            // wait for response
            String rawResponse = in.readLine();
            RegexGrammar response = new RegexGrammar(rawResponse);
            String type = response.getTypeOfCommand();

            // get correct task
            AuthTask task = switch (type) {
                case "USER_ACTION" -> loginTask;
                case "ONBOARDING" -> registerTask;
                default -> {
                    String error = new RegexGrammar().buildCommand("ERROR", "Invalid authentication command");
                    out.println(error);
                    throw new AuthentificationFailed("Invalid response");
                }
            };

            return task.authenticate(client, in, out, response, randomString);
        } catch (IOException e) {
            throw new AuthentificationFailed("Error getting connection to client");
        }
    }

    private String randomString() {
        String digits = "0123456789";
        String letters = "abcdefghijklmnopqrstuvwxyz";
        String specials = "!@#$%^";
        String all = digits + letters + letters.toUpperCase() + specials;

        Random r = new Random();
        char[] chars = new char[RANDOM_STRING_LENGTH];
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            chars[i] = all.charAt(r.nextInt(all.length()));
        }
        return new String(chars);
    }
}
