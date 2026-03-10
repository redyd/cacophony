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
import java.util.List;

public class RegisterTask implements AuthTask {

    private final UserRepo userRepo;

    @Inject
    public RegisterTask(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public ClientConnection authenticate(Socket client, BufferedReader in, PrintWriter out, RegexGrammar response, String randomChars) throws AuthentificationFailed {
        List<String> onboardingValues = response.parseCommand();

        if (onboardingValues.size() != 3) {
            String err = new RegexGrammar().buildCommand("ERROR", "Invalid response to welcome");
        }

        String login = onboardingValues.getFirst();
        int saltLength = Integer.parseInt(onboardingValues.get(1));
        String bcryptHash = onboardingValues.get(2);

        // TODO créer user

        return null;
    }

}
