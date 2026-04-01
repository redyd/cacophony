package com.mapatiri.node.task;

import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
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
    private ConfigRepository configRepository;
    private IdentityUtils identityUtils;

    private TaskConverter taskConverter;

    @BeforeEach
    void setup() {

        requestTaskManager = mock(RequestTaskManager.class);
        userRepo = mock(UserRepo.class);
        tagRepo = mock(TagRepo.class);
        connectionStore = mock(ConnectionStore.class);
        configRepository = mock(ConfigRepository.class);
        identityUtils = mock(IdentityUtils.class);


        NodeConfig config = mock(NodeConfig.class);
        when(config.getCurrentDomain()).thenReturn("node1.swilabus.xyz");
        when(configRepository.getConfig()).thenReturn(config);

        taskConverter = new TaskConverter(
                requestTaskManager,
                userRepo,
                tagRepo,
                connectionStore,
                configRepository,
                identityUtils
        );
    }

    @Test
    void should_create_send_task_from_post_msg() {

        String message =
                "POST 123@domainA alice1@domainA bob12@domainB MSG bob12@domainB hello_world\r\n";

        taskConverter.convertAndSend(message);

        ArgumentCaptor<ActionTask> captor = ArgumentCaptor.forClass(ActionTask.class);
        verify(requestTaskManager).submit(captor.capture());

        assertTrue(captor.getValue() instanceof SendTask);
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

        assertThrows(
                IllegalArgumentException.class,
                () -> taskConverter.convertAndSend(message)
        );
    }

    @Test
    void should_submit_only_one_task() {

        String message =
                "POST 111@domainA alice1@domainA bob12@domainB MSG bob12@domainB hello\r\n";

        taskConverter.convertAndSend(message);

        verify(requestTaskManager, times(1)).submit(any());
    }
}