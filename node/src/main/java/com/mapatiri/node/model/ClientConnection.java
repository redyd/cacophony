package com.mapatiri.node.model;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;

/**
 * Représente une connexion client ouverte et permet d'interagir facilement avec.
 */
public class ClientConnection {

    private final User user;
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public ClientConnection(Socket socket, User user, BufferedReader in, PrintWriter out) throws IOException {
        this.socket = socket;
        this.user = user;
        this.in = in;
        this.out = out;
    }

    public synchronized void send(String message) {
        out.println(message);
    }

    public synchronized String waitForMessage() throws IOException {
        return in.readLine();
    }

    public boolean isDisconnected() {
        return socket.isClosed();
    }

    public void close() throws IOException {
        socket.close();
    }

    public User getUser() {
        return user;
    }

}
