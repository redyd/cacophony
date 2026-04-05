package com.mapatiri.node.controller;

import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
import com.mapatiri.node.service.SpeakerBridgeService;
import com.mapatiri.node.task.ActionTask;
import com.mapatiri.node.task.SubscribeTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskConverterTest {
    private RequestTaskManager requestTaskManager;
    private UserRepo userRepo;
    private TagRepo tagRepo;
    private ConnectionStore connectionStore;
    private IdentityUtils identityUtils;
    private SpeakerBridgeService speakerBridgeService;

    private TaskConverter taskConverter;

    @BeforeEach
    void setup() {

        requestTaskManager = mock(RequestTaskManager.class);
        userRepo = mock(UserRepo.class);
        tagRepo = mock(TagRepo.class);
        connectionStore = mock(ConnectionStore.class);
        identityUtils = mock(IdentityUtils.class);
        speakerBridgeService = mock(SpeakerBridgeService.class);

        taskConverter = new TaskConverter(
                requestTaskManager,
                userRepo,
                tagRepo,
                connectionStore,
                identityUtils,
                speakerBridgeService
        );
    }

    @Test
    void should_create_send_task_from_post_msg() {
        ClientConnection connection = mock(ClientConnection.class);
        when(identityUtils.stripDomain("bob12@domainB")).thenReturn("bob12");
        when(connectionStore.get("bob12")).thenReturn(java.util.Optional.of(connection));
        when(connection.hasAlreadyReceived("123@domainA")).thenReturn(false);

        String message =
                "POST 123@domainA alice1@domainA bob12@domainB MSG bob12@domainB hello_world\r\n";

        taskConverter.convertAndSend(message);

        verify(connection).markAsReceived("123@domainA");
        verify(connection).send(contains("MSG alice1@domainA hello_world"));
        verifyNoInteractions(requestTaskManager);
    }

    @Test
    void should_create_subscribe_task_from_post_subscribe() {

        String message =
                "POST 456@domainA alice1@domainA bob12@domainB SUBSCRIBE bob12@domainB\r\n";

        taskConverter.convertAndSend(message);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertTrue(captor.getValue() instanceof SubscribeTask);
    }

    @Test
    void should_throw_exception_for_unknown_inner_command() {

        String message =
                "POST 789@domainA alice1@domainA bob12@domainB UNKNOWN something\r\n";

        assertDoesNotThrow(() -> taskConverter.convertAndSend(message));
        verifyNoInteractions(requestTaskManager);
    }

    @Test
    void should_submit_only_one_task() {

        String message =
                "POST 111@domainA alice1@domainA bob12@domainB SUBSCRIBE bob12@domainB\r\n";

        taskConverter.convertAndSend(message);

        verify(requestTaskManager, times(1)).submit(any());
    }
}
