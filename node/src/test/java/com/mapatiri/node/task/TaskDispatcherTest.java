package com.mapatiri.node.task;



import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.AuthTaskManager;
import com.mapatiri.node.service.ConnectionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import task.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskDispatcherTest {

    @Mock private RequestTaskManager requestTaskManager;
    @Mock private UserRepo userRepo;
    @Mock private TagRepo tagRepo;
    @Mock private ConnectionStore connectionStore;
    @Mock private AuthTaskManager authTaskManager;

    private TaskDispatcher dispatcher;
    private final String LOCAL_DOMAIN = "louis@node1.swilabus.xyz";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dispatcher = new TaskDispatcher(
                requestTaskManager, userRepo, tagRepo, connectionStore, authTaskManager
        );
    }

    // ===== SEND =====

    @Test
    void submit_sendCommand_createsSendTask() throws BadRequestException {
        dispatcher.submit("SEND Hey les amis!\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertInstanceOf(SendTask.class, captor.getValue());
    }

    @Test
    void submit_sendCommand_shouldForwardIsTrue() throws BadRequestException {
        dispatcher.submit("SEND Hey les amis!\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertTrue(captor.getValue().shouldForward());
    }

    @Test
    void submit_sendCommandWithTag_extractsTagsCorrectly() throws BadRequestException {
        dispatcher.submit("SEND Hey #metal11111 fans!\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        SendTask sendTask = (SendTask) captor.getValue();
        assertNotNull(sendTask);
    }

    // ===== MSG =====

    @Test
    void submit_msgCommand_createsSendTask() throws BadRequestException {
        dispatcher.submit("MSG louis@node1.swilabus.xyz Hey!\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertInstanceOf(SendTask.class, captor.getValue());
    }

    @Test
    void submit_msgCommand_shouldForwardIsFalse() throws BadRequestException {
        dispatcher.submit("MSG louis@node1.swilabus.xyz Hey!\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertFalse(captor.getValue().shouldForward());
    }

    // ===== SUBSCRIBE to local user =====

    @Test
    void submit_subscribeLocalUser_createsSubscribeTaskNotForwarded() throws BadRequestException {
        dispatcher.submit("SUBSCRIBE alice11111@node1.swilabus.xyz\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertInstanceOf(SubscribeTask.class, captor.getValue());
        assertFalse(captor.getValue().shouldForward());
    }

    // ===== SUBSCRIBE to distant user =====

    @Test
    void submit_subscribeDistantUser_createsSubscribeTaskForwarded() throws BadRequestException {
        dispatcher.submit("SUBSCRIBE alice11111@node2.swilabus.xyz\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertInstanceOf(SubscribeTask.class, captor.getValue());
        assertTrue(captor.getValue().shouldForward());
    }

    // ===== SUBSCRIBE to local tag =====

    @Test
    void submit_subscribeLocalTag_createsSubscribeTaskNotForwarded() throws BadRequestException {
        dispatcher.submit("SUBSCRIBE #metal11111@node1.swilabus.xyz\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertInstanceOf(SubscribeTask.class, captor.getValue());
        assertFalse(captor.getValue().shouldForward());
    }

    // ===== SUBSCRIBE to distant tag =====

    @Test
    void submit_subscribeDistantTag_createsSubscribeTaskForwarded() throws BadRequestException {
        dispatcher.submit("SUBSCRIBE #metal11111@node2.swilabus.xyz\r\n", LOCAL_DOMAIN);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertInstanceOf(SubscribeTask.class, captor.getValue());
        assertTrue(captor.getValue().shouldForward());
    }

    // ===== Invalid command =====

    @Test
    void submit_invalidCommand_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () ->
                dispatcher.submit("INVALID command\r\n", LOCAL_DOMAIN)
        );
    }

    @Test
    void submit_leaveCommand_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () ->
                dispatcher.submit("LEAVE\r\n", LOCAL_DOMAIN)
        );
    }

    @Test
    void submit_emptyCommand_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () ->
                dispatcher.submit("", LOCAL_DOMAIN)
        );
    }
}
