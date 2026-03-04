package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.User;
import com.mapatiri.node.service.ConnectionStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginTask implements AuthTask {

    private final ConnectionStore store;

    @Inject
    public LoginTask(ConnectionStore store) {
        this.store = store;
    }

    @Override
    public ClientConnection authenticate(Socket client, BufferedReader in, PrintWriter out) throws AuthentificationFailed {
        try {
            // TODO wait for USER
            String firstResponse = in.readLine();

            // TODO récupérer le nombre de rotation et le sel
            int rotation = 12;
            String sel = "hi twin je suis le sel";

            // TODO challenge (nombre de rotation et sel à utiliser)
            out.println("CHALLENGE %s %s".formatted(rotation, sel));

            // TODO récupérer RESPONSE
            String response = in.readLine();

            // TODO vérifier la réponse

            // TODO récupérer le user
            User user = null;

            ClientConnection connection = new ClientConnection(client, user);
            store.put(connection);

            return connection;
        } catch (IOException e) {
            // TODO gérer les erreurs
            out.println("ERROR");
            throw new AuthentificationFailed("Invalid response");
        }
    }

}
