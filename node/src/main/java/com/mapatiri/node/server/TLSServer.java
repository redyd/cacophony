package com.mapatiri.node.server;

import com.google.inject.Inject;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.exception.ServerSocketInitializer;
import com.mapatiri.node.factory.SocketFactory;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.service.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TLSServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TLSServer.class);

    private final SocketFactory tlsFactory;
    private final Dispatcher dispatcher;
    private final ConfigRepository config;

    @Inject
    public TLSServer(SocketFactory tlsFactory, Dispatcher dispatcher, ConfigRepository config) {
        this.tlsFactory = tlsFactory;
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    public void run() {
        try (ServerSocket socket = tlsFactory.createSocket()) {
            logger.info("[TLS is {}] Server Started on port {}", config.getConfig().isTls() ? "on" : "off", socket.getLocalPort());

            while (!socket.isClosed()) {
                Socket client = socket.accept();
                new Thread(() -> handleClient(client)).start();
            }
        } catch (IOException | ServerSocketInitializer e) {
            logger.error("Could not create server socket: {}", e.getMessage());
        }
    }

    private void handleClient(Socket sslSocket) {
        try {
            ClientConnection client = dispatcher.startAuth(sslSocket);

            while (!client.isDisconnected()) {
                try {
                    String message = client.waitForMessage();
                    dispatcher.submit(message, client.getUser().login());
                } catch (IOException e) {
                    logger.warn("Error while waiting for message: {}", e.getMessage());
                } catch (BadRequestException e) {
                    logger.info("User made a bad request: {}", e.getMessage());
                    client.send("ERROR");
                }
            }
        } catch (AuthentificationFailed e) {
            logger.info("An user could not be authenticated: {}", e.getMessage());
        }
    }

}
