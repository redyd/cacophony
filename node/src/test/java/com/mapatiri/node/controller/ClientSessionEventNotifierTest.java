package com.mapatiri.node.controller;

import com.mapatiri.node.model.ClientConnection;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClientSessionEventNotifierTest {

    @Test
    void notifiesSubscribersOnLoginAndLogout() {
        ClientSessionEventNotifier notifier = new ClientSessionEventNotifier();
        ClientSessionReacter reacter1 = mock(ClientSessionReacter.class);
        ClientSessionReacter reacter2 = mock(ClientSessionReacter.class);
        ClientConnection connection = mock(ClientConnection.class);

        notifier.subscribe(reacter1);
        notifier.subscribe(reacter2);

        notifier.onUserLogin(connection);
        notifier.onUserLogout(connection);

        verify(reacter1).onUserLogin(connection);
        verify(reacter2).onUserLogin(connection);
        verify(reacter1).onUserLogout(connection);
        verify(reacter2).onUserLogout(connection);
    }
}
