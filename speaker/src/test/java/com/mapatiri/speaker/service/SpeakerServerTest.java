package com.mapatiri.speaker.service;

import com.google.gson.Gson;
import com.mapatiri.speaker.model.NodeConnection;
import com.mapatiri.speaker.model.SpeakerConfig;
import com.mapatiri.speaker.repository.SpeakerConfigRepository;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpeakerServerTest {

    @Test
    void startConnectionForIgnoresUnknownDomain() {
        NodeStore nodeStore = mock(NodeStore.class);
        AESEncrypter aesEncrypter = mock(AESEncrypter.class);
        SpeakerConfigRepository repository = mock(SpeakerConfigRepository.class);
        when(repository.getConfig()).thenReturn(defaultConfig());

        SpeakerServer speakerServer = new SpeakerServer(nodeStore, aesEncrypter, repository);

        speakerServer.startConnectionFor(InetAddress.getLoopbackAddress(), "unknown.test", 4000);

        verify(nodeStore, never()).isConnected("unknown.test");
        verify(nodeStore, never()).Add(any(NodeConnection.class));
    }

    @Test
    void startConnectionForIgnoresAlreadyConnectedDomain() {
        NodeStore nodeStore = mock(NodeStore.class);
        AESEncrypter aesEncrypter = mock(AESEncrypter.class);
        SpeakerConfigRepository repository = mock(SpeakerConfigRepository.class);
        when(repository.getConfig()).thenReturn(defaultConfig());
        when(nodeStore.isConnected("source.test")).thenReturn(true);

        SpeakerServer speakerServer = new SpeakerServer(nodeStore, aesEncrypter, repository);

        speakerServer.startConnectionFor(InetAddress.getLoopbackAddress(), "source.test", 4000);

        verify(nodeStore).isConnected("source.test");
        verify(nodeStore, never()).Add(any(NodeConnection.class));
    }

    @Test
    void startConnectionForReadsDecryptsAndForwardsMessage() throws Exception {
        NodeStore nodeStore = mock(NodeStore.class);
        AESEncrypter aesEncrypter = mock(AESEncrypter.class);
        SpeakerConfigRepository repository = mock(SpeakerConfigRepository.class);
        when(repository.getConfig()).thenReturn(defaultConfig());
        when(nodeStore.isConnected("source.test")).thenReturn(false);

        String decryptedMessage = "POST 1@source.test alice1@source.test bob22@dest.test hello\r\n";
        when(aesEncrypter.decrypt("source.test", "encrypted-line")).thenReturn(decryptedMessage);
        when(aesEncrypter.encrypt("dest.test", decryptedMessage)).thenReturn("reencrypted-line");

        SpeakerServer speakerServer = new SpeakerServer(nodeStore, aesEncrypter, repository);

        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            int port = serverSocket.getLocalPort();
            Thread sender = new Thread(() -> sendOneLine(serverSocket, "encrypted-line"));
            sender.start();

            speakerServer.startConnectionFor(InetAddress.getLoopbackAddress(), "source.test", port);

            verify(nodeStore, timeout(2000)).Add(any(NodeConnection.class));
            verify(aesEncrypter, timeout(2000)).decrypt("source.test", "encrypted-line");
            verify(aesEncrypter, timeout(2000)).encrypt("dest.test", decryptedMessage);
            verify(nodeStore, timeout(2000)).broadcast("dest.test", "reencrypted-line");
            verify(nodeStore, timeout(2000)).remove("source.test");

            sender.join(2000);
        }
    }

    @Test
    void startConnectionForRemovesDomainWhenConnectionFails() throws Exception {
        NodeStore nodeStore = mock(NodeStore.class);
        AESEncrypter aesEncrypter = mock(AESEncrypter.class);
        SpeakerConfigRepository repository = mock(SpeakerConfigRepository.class);
        when(repository.getConfig()).thenReturn(defaultConfig());
        when(nodeStore.isConnected("source.test")).thenReturn(false);

        SpeakerServer speakerServer = new SpeakerServer(nodeStore, aesEncrypter, repository);

        int unusedPort;
        try (ServerSocket probe = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            unusedPort = probe.getLocalPort();
        }

        speakerServer.startConnectionFor(InetAddress.getLoopbackAddress(), "source.test", unusedPort);

        verify(nodeStore, timeout(2000)).remove("source.test");
    }

    private void sendOneLine(ServerSocket serverSocket, String line) {
        try (Socket accepted = serverSocket.accept();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(accepted.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (Exception ignored) {
        }
    }

    private SpeakerConfig defaultConfig() {
        String json = """
                {
                  "multicastAddress": "224.224.200.200",
                  "multicastPort": 11003,
                  "networkInterface": "eth0",
                  "configuredDomains": [
                    {
                      "domain": "source.test",
                      "base64AES": "1f2682Wgj/OGwmbUCBAH8fuCeKsBYz8E4o+HM4+50is="
                    },
                    {
                      "domain": "dest.test",
                      "base64AES": "x0o9NMfpjiRRrSWzwka+g8vc1zO1BJXcdo3jHCZD+6Y="
                    }
                  ]
                }
                """;
        return new Gson().fromJson(json, SpeakerConfig.class);
    }
}
