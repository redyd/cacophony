package com.mapatiri.speaker.service;

import com.mapatiri.speaker.model.NodeConnection;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NodeStoreTest {

    @Test
    void addAndRemoveTrackConnectionStateByDomain() {
        NodeStore nodeStore = new NodeStore();
        NodeConnection connection = mock(NodeConnection.class);
        when(connection.getDomain()).thenReturn("node1.swilabus.xyz");

        nodeStore.Add(connection);
        assertTrue(nodeStore.isConnected("node1.swilabus.xyz"));

        nodeStore.remove("node1.swilabus.xyz");
        assertFalse(nodeStore.isConnected("node1.swilabus.xyz"));
    }

    @Test
    void broadcastSendsMessageToConnectedDestination() throws Exception {
        NodeStore nodeStore = new NodeStore();
        NodeConnection connection = mock(NodeConnection.class);
        when(connection.getDomain()).thenReturn("node2.swilabus.xyz");
        nodeStore.Add(connection);

        nodeStore.broadcast("node2.swilabus.xyz", "encrypted-message");

        verify(connection).send("encrypted-message");
    }

    @Test
    void broadcastDoesNothingForUnknownDestination() throws Exception {
        NodeStore nodeStore = new NodeStore();
        NodeConnection connection = mock(NodeConnection.class);

        nodeStore.broadcast("unknown.swilabus.xyz", "encrypted-message");

        verify(connection, never()).send("encrypted-message");
    }

    @Test
    void broadcastSwallowsIOExceptionFromNodeConnection() throws Exception {
        NodeStore nodeStore = new NodeStore();
        NodeConnection connection = mock(NodeConnection.class);
        when(connection.getDomain()).thenReturn("node3.swilabus.xyz");
        doThrow(new IOException("write error")).when(connection).send("encrypted-message");
        nodeStore.Add(connection);

        nodeStore.broadcast("node3.swilabus.xyz", "encrypted-message");

        verify(connection).send("encrypted-message");
    }
}
