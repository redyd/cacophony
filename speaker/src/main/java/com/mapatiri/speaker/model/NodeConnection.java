package com.mapatiri.speaker.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NodeConnection {

    private static final Logger logger = LoggerFactory.getLogger(NodeConnection.class);

    private final String domain;
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    public NodeConnection(String domain, Socket socket) throws IOException {
        this.domain = domain;
        this.socket = socket;
        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    public void send(String message) throws IOException {
        out.write(message);
        out.newLine();
        out.flush();
    }

    public String waitForMessage() throws IOException {
        return in.readLine();
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    public void close() throws IOException {
        socket.close();
    }

    public String getDomain() {
        return domain;
    }
}
