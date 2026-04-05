package com.mapatiri.node.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NodeModelBasicsTest {

    @Test
    void nodeConfigGettersReturnJsonValues() {
        String json = """
                {
                  "currentDomain": "node1.swilabus.xyz",
                  "saltSizeInBytes": 16,
                  "multicastAddress": "224.0.0.1",
                  "multicastPort": 5000,
                  "unicastPort": 6000,
                  "relayPort": 7000,
                  "networkInterface": "eth0",
                  "base64AES": "abc==",
                  "tls": true,
                  "keystorePassword": "secret"
                }
                """;

        NodeConfig config = new Gson().fromJson(json, NodeConfig.class);

        assertEquals("node1.swilabus.xyz", config.getCurrentDomain());
        assertEquals(16, config.getSaltSizeInBytes());
        assertEquals("224.0.0.1", config.getMulticastAddress());
        assertEquals(5000, config.getMulticastPort());
        assertEquals(6000, config.getUnicastPort());
        assertEquals(7000, config.getRelayPort());
        assertEquals("eth0", config.getNetworkInterface());
        assertEquals("abc==", config.getBase64AES());
        assertTrue(config.isTls());
        assertEquals("secret", config.getKeystorePassword());
        assertEquals(Path.of("src", "main", "resources", "data", "keystore.p12"), NodeConfig.KEYSTORE_PATH);
    }

    @Test
    void userAndTagExposeExpectedState() {
        User user = new User("alice", "hash", 10, "salt");
        Tag tag = new Tag("#java", "node1.swilabus.xyz");
        tag.addSubscriber("alice@node1.swilabus.xyz");

        assertEquals("alice", user.getLogin());
        assertEquals("$2b$10$salthash", user.getHashedPassword());
        assertEquals("hash", user.getBcryptHash());
        assertEquals(10, user.getBcryptRound());
        assertEquals("salt", user.getBcryptSalt());
        assertTrue(tag.getSubscribers().contains("alice@node1.swilabus.xyz"));
    }

    @Test
    void recordsExposeWrappedValues() {
        User user = new User("alice", "hash", 10, "salt");
        Tag tag = new Tag("#java", "node1.swilabus.xyz");

        Users users = new Users(List.of(user));
        Tags tags = new Tags(List.of(tag));
        BcryptParams params = new BcryptParams(12, "h", "s");

        assertEquals(1, users.users().size());
        assertEquals(1, tags.tags().size());
        assertEquals(12, params.round());
        assertEquals("h", params.hash());
        assertEquals("s", params.salt());
    }

    @Test
    void clientConnectionTracksMessagesAndIds() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.isClosed()).thenReturn(false, true);

        BufferedReader in = new BufferedReader(new StringReader("hello\n"));
        StringWriter outBuffer = new StringWriter();
        PrintWriter out = new PrintWriter(outBuffer, true);

        ClientConnection connection = new ClientConnection(socket, new User("alice", "h", 10, "salt"), in, out);
        connection.send("PING");

        assertEquals("hello", connection.waitForMessage());
        assertFalse(connection.isDisconnected());
        assertFalse(connection.hasAlreadyReceived("1@domain"));

        connection.markAsReceived("1@domain");

        assertTrue(connection.hasAlreadyReceived("1@domain"));
        connection.purgeExpiredProcessedMessageIds(System.currentTimeMillis() + 5_001L);
        assertFalse(connection.hasAlreadyReceived("1@domain"));
        assertTrue(outBuffer.toString().contains("PING"));
        connection.close();
        assertTrue(connection.isDisconnected());
    }
}
