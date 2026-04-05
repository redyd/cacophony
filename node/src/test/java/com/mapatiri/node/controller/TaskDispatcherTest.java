package com.mapatiri.node.controller;

import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.AuthTaskManager;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
import com.mapatiri.node.task.ActionTask;
import com.mapatiri.node.task.SendTask;
import com.mapatiri.node.task.SubscribeTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskDispatcherTest {

    @Mock private RequestTaskManager requestTaskManager;
    @Mock private UserRepo userRepo;
    @Mock private TagRepo tagRepo;
    @Mock private ConnectionStore connectionStore;
    @Mock private AuthTaskManager authTaskManager;
    @Mock private ConfigRepository configRepository;
    @Mock private ClientSessionEventNotifier notifier;

    private IdentityUtils identityUtils;
    private TaskDispatcher dispatcher;

    private static final String LOCAL_DOMAIN = "node1.swilabus.xyz";
    private static final String LOGIN        = "louis";
    private static final String SENDER       = LOGIN + "@" + LOCAL_DOMAIN;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        identityUtils = new IdentityUtils();

        NodeConfig config = mock(NodeConfig.class);
        when(config.getCurrentDomain()).thenReturn(LOCAL_DOMAIN);
        when(configRepository.getConfig()).thenReturn(config);
        when(configRepository.buildAtDomain(LOGIN)).thenReturn(SENDER);

        dispatcher = new TaskDispatcher(
                requestTaskManager, userRepo, tagRepo, connectionStore,
                authTaskManager, configRepository, identityUtils, notifier
        );
    }

    // ─────────────────────────────────────────────
    // SEND
    // ─────────────────────────────────────────────

    @Nested
    class SendCommand {

        @Test
        void createsSendTask() throws BadRequestException {
            dispatcher.submit("SEND Hey les amis!\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertInstanceOf(SendTask.class, captor.getValue());
        }

        @Test
        void shouldForwardIsTrue() throws BadRequestException {
            dispatcher.submit("SEND Hey les amis!\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertTrue(captor.getValue().shouldForward());
        }

        @Test
        void createsSendTaskWithTag() throws BadRequestException {
            dispatcher.submit("SEND Hey #metal11111 fans!\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertInstanceOf(SendTask.class, captor.getValue());
        }
    }

    // ─────────────────────────────────────────────
    // MSG
    // ─────────────────────────────────────────────

    @Nested
    class MsgCommand {

        @Test
        void createsSendTask() throws BadRequestException {
            dispatcher.submit("MSG louis@node1.swilabus.xyz Hey!\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertInstanceOf(SendTask.class, captor.getValue());
        }

        @Test
        void shouldForwardIsFalse() throws BadRequestException {
            dispatcher.submit("MSG louis@node1.swilabus.xyz Hey!\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertFalse(captor.getValue().shouldForward());
        }
    }

    // ─────────────────────────────────────────────
    // SUBSCRIBE
    // ─────────────────────────────────────────────

    @Nested
    class SubscribeCommand {

        @Test
        void localUser_createsSubscribeTaskNotForwarded() throws BadRequestException {
            dispatcher.submit("SUBSCRIBE alice11111@node1.swilabus.xyz\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertInstanceOf(SubscribeTask.class, captor.getValue());
            assertFalse(captor.getValue().shouldForward());
        }

        @Test
        void distantUser_createsSubscribeTaskForwarded() throws BadRequestException {
            dispatcher.submit("SUBSCRIBE alice11111@node2.swilabus.xyz\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertInstanceOf(SubscribeTask.class, captor.getValue());
            assertTrue(captor.getValue().shouldForward());
        }

        @Test
        void localTag_createsSubscribeTaskNotForwarded() throws BadRequestException {
            dispatcher.submit("SUBSCRIBE #metal11111@node1.swilabus.xyz\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertInstanceOf(SubscribeTask.class, captor.getValue());
            assertFalse(captor.getValue().shouldForward());
        }

        @Test
        void distantTag_createsSubscribeTaskForwarded() throws BadRequestException {
            dispatcher.submit("SUBSCRIBE #metal11111@node2.swilabus.xyz\r\n", LOGIN);

            ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
            verify(requestTaskManager).submit(captor.capture());
            assertInstanceOf(SubscribeTask.class, captor.getValue());
            assertTrue(captor.getValue().shouldForward());
        }
    }

    // ─────────────────────────────────────────────
    // Invalid commands
    // ─────────────────────────────────────────────

    @Nested
    class InvalidCommands {

        @Test
        void invalidCommand_throwsBadRequestException() {
            assertThrows(BadRequestException.class, () ->
                    dispatcher.submit("INVALID command\r\n", LOGIN)
            );
        }

        @Test
        void emptyCommand_throwsBadRequestException() {
            assertThrows(BadRequestException.class, () ->
                    dispatcher.submit("", LOGIN)
            );
        }

        @Test
        void invalidCommand_doesNotSubmitTask() {
            try {
                dispatcher.submit("INVALID command\r\n", LOGIN);
            } catch (BadRequestException | IllegalArgumentException ignored) {}

            verifyNoInteractions(requestTaskManager);
        }
    }
}