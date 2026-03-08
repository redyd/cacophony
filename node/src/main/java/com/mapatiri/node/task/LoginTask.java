package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.User;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Permet d'authentifier un utilisateur déjà enregistré.
 */
public class LoginTask implements AuthTask {

    private final ConnectionStore store;
    private final UserRepo userRepository;

    @Inject
    public LoginTask(ConnectionStore store, UserRepo userRepository) {
        this.store = store;
        this.userRepository = userRepository;
    }

    @Override
    public ClientConnection authenticate(Socket client, BufferedReader in, PrintWriter out) throws AuthentificationFailed {
        try {
            // TODO wait for USER
            String userLogin = in.readLine();

            User target = this.userRepository.getUserFromLogin(userLogin);

            if (target == null) {
                out.println("Invalid credentials");
                throw new AuthentificationFailed("Invalid user login");
            }

            // TODO challenge (nombre de rotation et sel à utiliser)
            out.println("CHALLENGE %s %s".formatted(target.getBcryptRound(), target.getBcryptSalt()));

            // TODO récupérer RESPONSE
            String response = in.readLine();

            // TODO vérifier la réponse
            if (!response.equals("RESPONSE " + "not good credentials")) {
                out.println("Invalid credentials");
                throw new AuthentificationFailed("Login failed");
            }

            ClientConnection connection = new ClientConnection(client, target);
            store.put(connection);

            return connection;
        } catch (IOException e) {
            out.println("Something went wrong");
            throw new AuthentificationFailed("Something went wrong");
        }
    }

}
