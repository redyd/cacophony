package com.mapatiri.node.model;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Représente une connexion client ouverte et permet d'interagir facilement avec.
 *
 * @author Timëo
 * @author Riham
 */
public class ClientConnection {

    private final User user;
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Set<String> processedMessageIds = new HashSet<>();

    public ClientConnection(Socket socket, User user, BufferedReader in, PrintWriter out) throws IOException {
        this.socket = socket;
        this.user = user;
        this.in = in;
        this.out = out;
    }

    public  void send(String message) {
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

    public boolean hasAlreadyReceived(String idAtDomain) {
        return processedMessageIds.contains(idAtDomain);
    }

    public void markAsReceived(String idAtDomain) {
        processedMessageIds.add(idAtDomain);
    }

    public User getUser() {
        return user;
    }

}
