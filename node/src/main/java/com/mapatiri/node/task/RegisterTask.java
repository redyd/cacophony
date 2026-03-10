package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.UserRepo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterTask implements AuthTask {

    private final UserRepo userRepo;

    @Inject
    public RegisterTask(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public ClientConnection authenticate(Socket client, BufferedReader in, PrintWriter out, RegexGrammar response, String randomChars) throws AuthentificationFailed {
        try {
            // TODO récupérer les informations du onboarding
            String onboarding = in.readLine();
            String login = "login from onboarding";
            int saltLength = 67;
            String bcryptHash = "hashed password from onboarding";

            // TODO créer user

            return null;
        } catch (IOException e) {
            out.println("Something went wrong");
            throw new AuthentificationFailed("Something went wrong");
        }
    }

}
