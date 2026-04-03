package com.mapatiri.node.task;

import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.service.ConnectionStore;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LeaveTaskTest {

    @Test
    void executeTaskNotifiesLogoutWhenUserIsConnected() {
        ClientSessionEventNotifier notifier = mock(ClientSessionEventNotifier.class);
        ConnectionStore store = mock(ConnectionStore.class);
        ClientConnection connection = mock(ClientConnection.class);
        when(store.get("alice")).thenReturn(Optional.of(connection));

        LeaveTask task = new LeaveTask(notifier, store, "alice");
        task.executeTask();

        verify(notifier).onUserLogout(connection);
        assertFalse(task.shouldForward());
    }

    @Test
    void executeTaskDoesNothingWhenUserIsNotConnected() {
        ClientSessionEventNotifier notifier = mock(ClientSessionEventNotifier.class);
        ConnectionStore store = mock(ConnectionStore.class);
        when(store.get("alice")).thenReturn(Optional.empty());

        LeaveTask task = new LeaveTask(notifier, store, "alice");
        task.executeTask();

        verifyNoInteractions(notifier);
    }
}
