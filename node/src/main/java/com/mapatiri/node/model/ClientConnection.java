package com.mapatiri.node.model;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, Long> processedMessageIds = new HashMap<>();
    private static final long PROCESSED_ID_TTL_MS = 5_000L;

    public ClientConnection(Socket socket, User user, BufferedReader in, PrintWriter out) throws IOException {
        this.socket = socket;
        this.user = user;
        this.in = in;
        this.out = out;
    }

    public void send(String message) {
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

    public synchronized boolean hasAlreadyReceived(String idAtDomain) {
        return processedMessageIds.containsKey(idAtDomain);
    }

    public synchronized void markAsReceived(String idAtDomain) {
        processedMessageIds.put(idAtDomain, System.currentTimeMillis());
    }

    public synchronized void purgeExpiredProcessedMessageIds() {
        purgeExpiredProcessedMessageIds(System.currentTimeMillis());
    }

    synchronized void purgeExpiredProcessedMessageIds(long now) {
        processedMessageIds
                .entrySet()
                .removeIf(entry -> now - entry.getValue() > PROCESSED_ID_TTL_MS);
    }

    public User getUser() {
        return user;
    }

}
