package com.mapatiri.node;

import config.JsonServiceImpl;
import config.NodeConfigSaveException;
import config.NodeFileException;
import model.BcryptParams;
import model.NodeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JsonServiceImplTest {


    @TempDir
    Path tempDir;
    JsonServiceImpl service;

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
        service = new JsonServiceImpl("node-config-test.json");
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
        assertEquals(22, service.getUser("louis").get().getSaltLenght());
    }


    @Test
    void testParseBcrypt() {
        // Énoncé TP : salt_length=22
        String bcrypt = "$2b$14$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLmnopqrstuvwxyzABCDEF";
        BcryptParams params = service.parseByCrypt(bcrypt);

        assertEquals(14, params.rotationNbr());
        assertEquals("abcdefghijklmnopqrstuv", params.saltLenght());
        assertEquals(22, params.saltLenght().length());
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
