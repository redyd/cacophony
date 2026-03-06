package com.mapatiri.node.server;

import com.google.inject.Inject;
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

    @Inject
    public ClientNodeServer(SocketFactory tlsFactory, Dispatcher dispatcher, ConfigRepository config) {
        this.tlsFactory = tlsFactory;
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    public void run() {
        try (ServerSocket socket = tlsFactory.createSocket()) {
            logger.info("> Server client started on port {} (tls {})", socket.getLocalPort(), config.getConfig().isTls() ? "on" : "off");

            while (!socket.isClosed()) {
                Socket client = socket.accept();

                if (client instanceof SSLSocket sslSocket) {
                    sslSocket.startHandshake();
                }

                new Thread(() -> handleClient(client), "Running client").start();
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
                    dispatcher.submit(message, client.getUser().getLogin());
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
