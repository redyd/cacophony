package com.mapatiri.node.model;

import lombok.Getter;

import javax.net.ssl.SSLSocket;
import java.io.*;

/**
 * Représente une connexion client ouverte et permet d'interagir facilement avec.
 */
public class ClientConnection {

    @Getter
    private final User user;

    private final SSLSocket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public ClientConnection(SSLSocket socket, User user) throws IOException {
        this.socket = socket;
        this.user = user;

        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );
        this.out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())
                ), true
        );
    }

    public synchronized void send(String message) {
        out.println(message);
    }

    public synchronized String waitForMessage() throws IOException {
        return in.readLine();
    }

    public boolean isDisconnected() {
        return !socket.isConnected()
                || socket.isClosed()
                || socket.isInputShutdown()
                || socket.isOutputShutdown();
    }

    public void close() throws IOException {
        socket.close();
    }

}
