package com.mapatiri.node.factory;

import com.google.gson.Gson;
import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import org.junit.jupiter.api.Test;

import java.net.MulticastSocket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UDPFactoryTest {

    @Test
    void createReturnsSocketEvenWhenNetworkInterfaceIsUnknown() throws Exception {
        ConfigRepository repository = mock(ConfigRepository.class);
        when(repository.getConfig()).thenReturn(config(0, "unknown-interface"));
        UDPFactory factory = new UDPFactory(repository);

        try (MulticastSocket socket = factory.create()) {
            assertNotNull(socket);
        }
    }

    @Test
    void createThrowsSocketCreationForInvalidPort() {
        ConfigRepository repository = mock(ConfigRepository.class);
        when(repository.getConfig()).thenReturn(config(-1, "unknown-interface"));
        UDPFactory factory = new UDPFactory(repository);

        assertThrows(SocketCreation.class, factory::create);
    }

    private NodeConfig config(int multicastPort, String networkInterface) {
        String json = """
                {
                  "currentDomain": "node1.swilabus.xyz",
                  "saltSizeInBytes": 16,
                  "multicastAddress": "224.0.0.1",
                  "multicastPort": %d,
                  "unicastPort": 6000,
                  "relayPort": 7000,
                  "networkInterface": "%s",
                  "base64AES": "abc==",
                  "tls": false,
                  "keystorePassword": "secret"
                }
                """.formatted(multicastPort, networkInterface);
        return new Gson().fromJson(json, NodeConfig.class);
    }
}
