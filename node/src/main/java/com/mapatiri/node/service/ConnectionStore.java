package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.controller.ClientSessionReacter;
import com.mapatiri.node.model.ClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Enregistre toutes les connexions actives et authentifiées dans le node
 */
public class ConnectionStore implements Clockable, ClientSessionReacter {

    private final static Logger logger = LoggerFactory.getLogger(ConnectionStore.class);

    private final Map<String, ClientConnection> connections;

    @Inject
    public ConnectionStore(ClientSessionEventNotifier notifier) {
        connections = new HashMap<>();
        notifier.subscribe(this);
    }

    public void put(ClientConnection client) {
        if (client.isDisconnected()) {
            logger.warn("Client connection has been closed");
            return;
        }

        String login = client.getUser().getLogin();

        try {
            if (connections.containsKey(login)) {
                connections.get(login).close();
            }
        } catch (IOException e) {
            logger.warn("An error occured while closing existing connection for '{}'", login);
        }

        connections.put(login, client);
    }

    public Optional<ClientConnection> get(String login) {
        return Optional.ofNullable(connections.get(login));
    }

    @Override
    public void onClockSignal() {
        Set<String> toRemove = new HashSet<>();

        for (Map.Entry<String, ClientConnection> entry : connections.entrySet()) {
            ClientConnection client = entry.getValue();
            try {
                if (client.isDisconnected()) {
                    client.close();
                    toRemove.add(entry.getKey());
                }
            } catch (IOException e) {
                logger.warn("An error occurred while closing the connection for '{}'", client.getUser().getLogin(), e);
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(connections::remove);
    }

    @Override
    public void onUserLogout(ClientConnection client) {
        connections.remove(client.getUser().getLogin());
        logger.info("Client {} removed from store", client.getUser().getLogin());
    }

    @Override
    public void onUserLogin(ClientConnection client) {
        put(client);
        logger.info("Client {} added to store", client.getUser().getLogin());
    }
    public Set<ClientConnection> getConnectionsFromLogin(Set<String> logins) {
        Set<ClientConnection> connectionsResult = new HashSet<>();
        for (String login : logins) {
           var con =   connections.get(login);
            if (con != null) {
                connectionsResult.add(con);
            }
        }
        return connectionsResult;
    }
}
