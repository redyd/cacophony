package com.mapatiri.node.factory;

import com.google.gson.Gson;
import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SocketFactoryTest {

    @Test
    void createSocketReturnsPlainServerSocketWhenTlsDisabled() throws Exception {
        ConfigRepository repository = mock(ConfigRepository.class);
        when(repository.getConfig()).thenReturn(config(false, "secret"));
        SocketFactory socketFactory = new SocketFactory(repository);

        try (ServerSocket socket = socketFactory.createSocket()) {
            assertNotNull(socket);
        }
    }

    @Test
    void createSocketThrowsWhenTlsEnabledWithInvalidPassword() {
        ConfigRepository repository = mock(ConfigRepository.class);
        when(repository.getConfig()).thenReturn(config(true, "invalid-password"));
        SocketFactory socketFactory = new SocketFactory(repository);

        assertThrows(SocketCreation.class, socketFactory::createSocket);
    }

    private NodeConfig config(boolean tls, String password) {
        String json = """
                {
                  "currentDomain": "node1.swilabus.xyz",
                  "saltSizeInBytes": 16,
                  "multicastAddress": "224.0.0.1",
                  "multicastPort": 5000,
                  "unicastPort": 0,
                  "relayPort": 7000,
                  "networkInterface": "eth0",
                  "base64AES": "abc==",
                  "tls": %s,
                  "keystorePassword": "%s"
                }
                """.formatted(tls, password);
        return new Gson().fromJson(json, NodeConfig.class);
    }
}
