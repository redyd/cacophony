package com.mapatiri.node.task;



import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.model.Tag;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

    class SubscribeTaskTest {

        @Mock private UserRepo userRepo;
        @Mock private TagRepo tagRepo;
        @Mock private ConfigRepository configRepository;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            NodeConfig config = mock(NodeConfig.class);
            when(config.getCurrentDomain()).thenReturn("node1.swillabus.xyz");

            when(configRepository.getConfig()).thenReturn(config);
        }

        // ===== executeTask with TAG =====

        @Test
        void executeTask_tag_addsTagAndSubscriber() throws IOException {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "#metal11111@node1.xyz",
                    false
            );

            task.executeTask();

            verify(tagRepo).addTag(any(Tag.class));
            verify(tagRepo).addSubscriberToTag("#metal11111", "louis@node1.xyz");
            verify(userRepo).addUserTag("louis", "#metal11111@node1.xyz");
        }

        @Test
        void executeTask_tag_doesNotCallAddSubscriberToUser() throws IOException {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "#metal11111@node1.xyz",
                    false
            );

            task.executeTask();

            verify(userRepo, never()).addSubscriberToUser(anyString(), anyString());
        }

        // ===== executeTask with USER =====

        @Test
        void executeTask_user_addsSubscriberToUser() throws IOException {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "alice11111@node1.xyz",
                    false
            );

            task.executeTask();

            verify(userRepo).addSubscriberToUser( "louis", "alice11111@node1.xyz");
        }

        @Test
        void executeTask_user_doesNotCallTagRepo() throws IOException {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "alice11111@node1.xyz",
                    false
            );

            task.executeTask();

            verify(tagRepo, never()).addTag(any());
            verify(tagRepo, never()).addSubscriberToTag(anyString(), anyString());
            verify(userRepo, never()).addUserTag(anyString(), anyString());
        }

        @Test
        void executeTask_ioException_doesNotThrow() throws IOException {
            doThrow(new IOException("disk error"))
                    .when(tagRepo).addTag(any());

            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "#metal11111@node1.xyz",
                    false
            );

            // should catch IOException internally and log it
            assertDoesNotThrow(task::executeTask);
        }

        // ===== shouldForward =====

        @Test
        void shouldForward_localDomain_returnsFalse() {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "alice11111@node1.xyz",
                    false
            );
            assertFalse(task.shouldForward());
        }

        @Test
        void shouldForward_distantDomain_returnsTrue() {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "alice11111@node2.xyz",
                    true
            );
            assertTrue(task.shouldForward());
        }

        // ===== getters =====

        @Test
        void getSender_returnsCorrectSender() {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "alice11111@node1.xyz",
                    false
            );
            assertEquals("louis@node1.xyz", task.getSender());
        }

        @Test
        void getToSubscribe_returnsCorrectTarget() {
            SubscribeTask task = new SubscribeTask(
                    userRepo, tagRepo,
                    "louis@node1.xyz",
                    "#metal11111@node1.xyz",
                    false
            );
            assertEquals("#metal11111@node1.xyz", task.getToSubscribe());
        }
    }

