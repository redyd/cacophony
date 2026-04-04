package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.ErrorProtocole;
import com.mapatiri.grammar.parse.Ok;
import com.mapatiri.grammar.parse.Onboarding;
import com.mapatiri.grammar.parse.ParsedCommand;
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
    public ClientConnection authenticate(Socket client, BufferedReader in, PrintWriter out, ParsedCommand parsed, String randomChars) throws AuthentificationFailed {
        try {
            //List<String> onboardingValues = response.parseCommand();


            // check for response validity
            if (!(parsed instanceof Onboarding onboarding)) {
                String err = new RegexGrammar().build(new ErrorProtocole("Invalid response to welcome"));
                out.println(err);
                throw new AuthentificationFailed("Invalid response to welcome");
            }

            // get ready to init user
            String login = onboarding.user();
            int saltLength = Integer.parseInt( onboarding.saltLength());
            BcryptParams bcrypt = bcryptService.parseByCrypt(onboarding.bcryptHash(), saltLength);

            // create & add user to the repository
            User user = initUser(out, login, bcrypt);
            ClientConnection connection = new ClientConnection(client, user, in, out);

            // create response

            String ok = new RegexGrammar()
                    .build(new Ok( "Welcome to Cacophony %s".formatted(connection.getUser().getLogin())));
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
            String err = new RegexGrammar().build(new ErrorProtocole("This login already exists"));
            out.println(err);
            throw new AuthentificationFailed("Could not create user with same login as another");
        }
        return user;
    }

}
