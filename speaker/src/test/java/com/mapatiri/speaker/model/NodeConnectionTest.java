package com.mapatiri.speaker.model;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeConnectionTest {

    @Test
    void sendWritesLineToSocket() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             Socket clientSocket = new Socket(InetAddress.getLoopbackAddress(), serverSocket.getLocalPort());
             ExecutorService executor = Executors.newSingleThreadExecutor()) {

            Future<String> received = executor.submit(() -> {
                try (Socket accepted = serverSocket.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(accepted.getInputStream(), StandardCharsets.UTF_8))) {
                    return reader.readLine();
                }
            });

            NodeConnection connection = new NodeConnection("node1.swilabus.xyz", clientSocket);
            connection.send("hello");

            assertEquals("hello", received.get(2, TimeUnit.SECONDS));
            executor.shutdownNow();
        }
    }

    @Test
    void waitForMessageReadsLineFromSocket() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             Socket clientSocket = new Socket(InetAddress.getLoopbackAddress(), serverSocket.getLocalPort());
             ExecutorService executor = Executors.newSingleThreadExecutor()) {

            executor.submit((Callable<Void>) () -> {
                try (Socket accepted = serverSocket.accept();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(accepted.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write("pong");
                    writer.newLine();
                    writer.flush();
                }
                return null;
            });

            NodeConnection connection = new NodeConnection("node1.swilabus.xyz", clientSocket);

            assertEquals("pong", connection.waitForMessage());
            assertEquals("node1.swilabus.xyz", connection.getDomain());
            executor.shutdownNow();
        }
    }

    @Test
    void isConnectedReflectsSocketState() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             Socket clientSocket = new Socket(InetAddress.getLoopbackAddress(), serverSocket.getLocalPort());
             Socket ignored = serverSocket.accept()) {

            NodeConnection connection = new NodeConnection("node1.swilabus.xyz", clientSocket);

            assertTrue(connection.isConnected());

            connection.close();

            assertFalse(connection.isConnected());
        }
    }
}
