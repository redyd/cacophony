package com.mapatiri.node.service;

import com.mapatiri.node.exception.NodeConfigSaveException;
import com.mapatiri.node.exception.NodeFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonServiceTest {


    @TempDir
    Path tempDir;
    JsonService service;

    @BeforeEach
    void setUp() throws Exception {
//        Path configFile = tempDir.resolve("test-config.json");
//        Files.writeString(configFile, """
//            {
//              "domain": "test.local",
//              "tcpClientPort": 8080,
//              "udpMulticastPort": 40001,
//              "multicastAddress": "224.224.200.200",
//              "aesSpeakerKey": "test",
//              "tlsKeystore": "keystore.p12",
//              "tlsKeystorePassword": "changeit",
//              "users": {},
//              "subscriptions": {}
//            }
//            """);

        service = new JsonService("node-config-test.json");
    }

    @Test
    void testLoadConfigValide() throws NodeFileException {
        service.loadNodeConfig();
        assertNotNull(service.loadNodeConfig());
    }

    @Test
    void testAddUser() throws NodeConfigSaveException {
        service.addUser("louis", 22, "$2b$14$test");
        assertTrue(service.getUser("louis").isPresent());
        assertEquals(22, service.getUser("louis").get().saltLength());
    }

    @Test
    void testSubscribeUser() throws NodeConfigSaveException {
        service.addSubscription("louis", "#java");
        service.addSubscription("louis", "#java"); // doublon
        List<String> subs = service.getSubscribers("louis");
        assertEquals(1, subs.size());
        assertTrue(subs.contains("#java"));
    }
}
