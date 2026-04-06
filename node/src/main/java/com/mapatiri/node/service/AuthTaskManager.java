package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.*;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.task.AuthTask;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;

import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;

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
            String welcome = new RegexGrammar().build(new Welcome(config.getCurrentDomain(), randomString));

            out.println(welcome);

            // wait for response
            String rawResponse = in.readLine();

            if (rawResponse == null) {
                throw new AuthentificationFailed("Response is empty");
            }

            RegexGrammar regexGrammar = new RegexGrammar();
            ParsedCommand parsedCommand = regexGrammar.parse(rawResponse);

            // get correct task
            AuthTask task = switch (parsedCommand) {
                case UserAction ignored -> loginTask;
                case Onboarding ignored -> registerTask;
                default -> {
                    String error = new RegexGrammar().build(new ErrorProtocole("Invalid authentication command"));
                    out.println(error);
                    throw new AuthentificationFailed("Invalid response");
                }
            };

            return task.authenticate(client, in, out, parsedCommand, randomString);
        } catch (IOException e) {
            throw new AuthentificationFailed("ErrorProtocole getting connection to client");
        }
    }

    private String randomString() {
        String digits = "0123456789";
        String letters = "abcdefghijklmnopqrstuvwxyz";
        String specials = "!@#$%^";
        String all = digits + letters + letters.toUpperCase() + specials;

        SecureRandom r = new SecureRandom();
        char[] chars = new char[RANDOM_STRING_LENGTH];
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            chars[i] = all.charAt(r.nextInt(all.length()));
        }
        return new String(chars);
    }
}
