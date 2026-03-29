package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.UserConflictException;
import com.mapatiri.node.model.BcryptParams;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.User;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.BcryptService;
import com.mapatiri.node.service.SimpleBcryptService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Tâche de création et d'authentification.
 *
 * @author Timëo
 */
public class RegisterTask implements AuthTask {

    private final UserRepo userRepo;
    private final BcryptService bcryptService;

    @Inject
    public RegisterTask(UserRepo userRepo, SimpleBcryptService simpleBcryptService) {
        this.userRepo = userRepo;
        this.bcryptService = simpleBcryptService;
    }

    @Override
    public ClientConnection authenticate(Socket client, BufferedReader in, PrintWriter out, RegexGrammar response, String randomChars) throws AuthentificationFailed {
        try {
            List<String> onboardingValues = response.parseCommand();

            // check for response validity
            if (onboardingValues.size() != 3 || !"ONBOARDING".equals(response.getTypeOfCommand())) {
                String err = new RegexGrammar().buildCommand("ERROR", "Invalid response to welcome");
                out.println(err);
                throw new AuthentificationFailed("Invalid response to welcome");
            }

            // get ready to init user
            String login = onboardingValues.getFirst();
            int saltLength = Integer.parseInt(onboardingValues.get(1));
            BcryptParams bcrypt = bcryptService.parseByCrypt(onboardingValues.get(2), saltLength);

            // create & add user to the repository
            User user = initUser(out, login, bcrypt);
            ClientConnection connection = new ClientConnection(client, user, in, out);

            // create response
            String ok = new RegexGrammar()
                    .buildCommand("OK", "Welcome to Cacophony %s".formatted(connection.getUser().getLogin()));
            connection.send(ok);

            return connection;
        } catch (IOException | NumberFormatException e) {
            throw new AuthentificationFailed("Unexpected error: " + e.getMessage());
        }
    }

    private User initUser(PrintWriter out, String login, BcryptParams bcrypt) throws IOException, AuthentificationFailed {
        User user = new User(login, bcrypt.hash(), bcrypt.round(), bcrypt.salt());
        try {
            userRepo.addUser(user);
        } catch (UserConflictException e) {
            String err = new RegexGrammar().buildCommand("ERROR", "This login already exists");
            out.println(err);
            throw new AuthentificationFailed("Could not create user with same login as another");
        }
        return user;
    }

}
