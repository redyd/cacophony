package com.mapatiri.node.server;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.exception.ServerSocketInitializer;
import com.mapatiri.node.factory.SocketFactory;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.controller.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientNodeServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientNodeServer.class);

    private final SocketFactory tlsFactory;
    private final Dispatcher dispatcher;
    private final ConfigRepository config;
    private final ClientSessionEventNotifier notifier;

    @Inject
    public ClientNodeServer(SocketFactory tlsFactory, Dispatcher dispatcher, ConfigRepository config, ClientSessionEventNotifier notifier) {
        this.tlsFactory = tlsFactory;
        this.dispatcher = dispatcher;
        this.config = config;
        this.notifier = notifier;
    }

    @Override
    public void run() {
        try (ServerSocket socket = tlsFactory.createSocket()) {
            logger.info(" ** Server client started on port {} (tls {}) ** ", socket.getLocalPort(), config.getConfig().isTls() ? "on" : "off");

            while (!socket.isClosed()) {
                try {
                    Socket client = socket.accept();

                    if (client instanceof SSLSocket sslSocket) {
                        sslSocket.startHandshake();
                    }

                    new Thread(() -> handleClient(client)).start();
                } catch (IOException e) {
                    logger.info("A user socket has encountered an error: {}", e.getMessage());
                }
            }
        } catch (ServerSocketInitializer | IOException e) {
            logger.error("Could not create server socket: {}", e.getMessage());
        }
    }

    private void handleClient(Socket sslSocket) {
        try {
            ClientConnection client = dispatcher.startAuth(sslSocket);
            Thread.currentThread().setName("Client-" + client.getUser().getLogin());
            logger.info("Client {} is log in", client.getUser().getLogin());
            this.notifier.onUserLogin(client);

            while (!client.isDisconnected()) {
                try {
                    String message = client.waitForMessage();

                    if (message == null) {
                        break;
                    }
                    if (message.isBlank()) {
                        continue;
                    }

                    dispatcher.submit(message, client.getUser().getLogin());
                } catch (IOException e) {
                    logger.warn("Error while waiting for message: {}", e.getMessage());
                    break;
                } catch (BadRequestException e) {
                    logger.info("User made a bad request: {}", e.getMessage());
                    String badRequest = new RegexGrammar().buildCommand("ERROR", "Bad Request");
                    client.send(badRequest);
                }
            }

            logger.info("Client {} log off", client.getUser().getLogin());
            this.notifier.onUserLogout(client);
        } catch (AuthentificationFailed e) {
            logger.info("Someone could not be authenticated: {}", e.getMessage());
        }
    }

}
