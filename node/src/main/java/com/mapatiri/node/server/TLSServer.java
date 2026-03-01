package com.mapatiri.node.server;

import com.google.inject.Inject;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.exception.TLSException;
import com.mapatiri.node.factory.TLSContextFactory;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.service.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class TLSServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TLSServer.class);

    private final TLSContextFactory tlsFactory;
    private final Dispatcher dispatcher;

    @Inject
    public TLSServer(TLSContextFactory tlsFactory, Dispatcher dispatcher) {
        this.tlsFactory = tlsFactory;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try {
            SSLContext context = tlsFactory.createContext();
            SSLServerSocketFactory factory = context.getServerSocketFactory();

            try (SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket()) {
                logger.info("TLS Server Started on port {}", socket.getLocalPort());

                while (!socket.isClosed()) {
                    SSLSocket sslSocket = (SSLSocket) socket.accept();
                    new Thread(() -> handleClient(sslSocket)).start();
                }
            } catch (IOException e) {
                logger.error("Could not create server socket: {}", e.getMessage());
            }

        } catch (TLSException e) {
            logger.error("Error creating TLS context: {}", e.getMessage(), e);
        }
    }

    private void handleClient(SSLSocket sslSocket) {
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
                    client.send("ERROR"); // TODO utiliser la grammaire
                }
            }
        } catch (AuthentificationFailed e) {
            logger.info("An user could not be authenticated: {}", e.getMessage());
        }
    }

}
