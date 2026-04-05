package com.mapatiri.node.service;

import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.User;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectionStoreTest {

    @Test
    void putAndGetStoreConnectedClient() {
        ConnectionStore store = new ConnectionStore(new ClientSessionEventNotifier());
        ClientConnection connection = mock(ClientConnection.class);
        User user = new User("alice", "h", 10, "salt");

        when(connection.getUser()).thenReturn(user);
        when(connection.isDisconnected()).thenReturn(false);

        store.put(connection);

        Optional<ClientConnection> result = store.get("alice");
        assertTrue(result.isPresent());
        assertEquals(connection, result.get());
    }

    @Test
    void putReplacesExistingClientWithSameLogin() throws Exception {
        ConnectionStore store = new ConnectionStore(new ClientSessionEventNotifier());
        ClientConnection oldConnection = mock(ClientConnection.class);
        ClientConnection newConnection = mock(ClientConnection.class);
        User user = new User("alice", "h", 10, "salt");

        when(oldConnection.getUser()).thenReturn(user);
        when(newConnection.getUser()).thenReturn(user);
        when(oldConnection.isDisconnected()).thenReturn(false);
        when(newConnection.isDisconnected()).thenReturn(false);

        store.put(oldConnection);
        store.put(newConnection);

        verify(oldConnection).close();
        assertEquals(newConnection, store.get("alice").orElseThrow());
    }

    @Test
    void onClockSignalRemovesDisconnectedClients() throws Exception {
        ConnectionStore store = new ConnectionStore(new ClientSessionEventNotifier());
        ClientConnection connection = mock(ClientConnection.class);
        User user = new User("alice", "h", 10, "salt");
        when(connection.getUser()).thenReturn(user);
        when(connection.isDisconnected()).thenReturn(false, true);

        store.put(connection);
        store.onClockSignal();

        verify(connection).close();
        assertFalse(store.get("alice").isPresent());
    }

    @Test
    void onClockSignalRemovesClientWhenCloseThrows() throws Exception {
        ConnectionStore store = new ConnectionStore(new ClientSessionEventNotifier());
        ClientConnection connection = mock(ClientConnection.class);
        User user = new User("alice", "h", 10, "salt");
        when(connection.getUser()).thenReturn(user);
        when(connection.isDisconnected()).thenReturn(false, true);
        doThrow(new IOException("boom")).when(connection).close();

        store.put(connection);
        store.onClockSignal();

        assertFalse(store.get("alice").isPresent());
    }

    @Test
    void getConnectionsFromLoginReturnsOnlyExistingOnes() {
        ConnectionStore store = new ConnectionStore(new ClientSessionEventNotifier());
        ClientConnection connection = mock(ClientConnection.class);
        User user = new User("alice", "h", 10, "salt");

        when(connection.getUser()).thenReturn(user);
        when(connection.isDisconnected()).thenReturn(false);
        store.put(connection);

        Set<ClientConnection> result = store.getConnectionsFromLogin(Set.of("alice", "bob"));
        assertEquals(1, result.size());
        assertTrue(result.contains(connection));
    }
}
