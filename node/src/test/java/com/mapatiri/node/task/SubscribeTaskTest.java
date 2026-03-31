package com.mapatiri.node.task;

import com.mapatiri.node.model.Tag;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.IdentityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscribeTaskTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private TagRepo tagRepo;

    private IdentityUtils identityUtils;

    @BeforeEach
    void setUp() {
        identityUtils = new IdentityUtils();
    }

    // ===================== subscribeToTag =====================

    @Test
    void executeTask_subscribeToNewTag_shouldCreateTagAndAddSubscriber() throws IOException {
        Tag expectedTag = new Tag("#monTag1", "node1.swilabus.xyz");
        when(tagRepo.tagExist(any(Tag.class))).thenReturn(false);

        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "louis@node1.swilabus.xyz",
                "#monTag1@node1.swilabus.xyz",
                false
        );

        task.executeTask();

        verify(tagRepo).addTag(argThat(tag ->
                tag.getTag().equals("#monTag1") &&
                        tag.getDomain().equals("node1.swilabus.xyz")
        ));
        verify(tagRepo).addSubscriberToTag("#monTag1", "louis@node1.swilabus.xyz");
    }

    @Test
    void executeTask_subscribeToExistingTag_shouldNotCreateTagAgain() throws IOException {
        when(tagRepo.tagExist(any(Tag.class))).thenReturn(true);

        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "louis@node1.swilabus.xyz",
                "#monTag1@node1.swilabus.xyz",
                false
        );

        task.executeTask();

        verify(tagRepo, never()).addTag(any(Tag.class)); // ← tag déjà existant, pas recréé
        verify(tagRepo).addSubscriberToTag("#monTag1", "louis@node1.swilabus.xyz");
    }

    @Test
    void executeTask_subscribeToTag_shouldNotTouchUserRepo() throws IOException {
        when(tagRepo.tagExist(any(Tag.class))).thenReturn(false);

        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "louis@node1.swilabus.xyz",
                "#monTag1@node1.swilabus.xyz",
                false
        );

        task.executeTask();

        verifyNoInteractions(userRepo); // ← plus de addUserTag
    }

    // ===================== subscribeToUser =====================

    @Test
    void executeTask_subscribeToUser_shouldAddSubscriberToUser() throws IOException {
        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "swawa@node1.swilabus.xyz",
                "louis@node1.swilabus.xyz",
                false
        );

        task.executeTask();

        verify(userRepo).addSubscriberToUser("louis", "swawa@node1.swilabus.xyz");
        verifyNoInteractions(tagRepo); // ← pas de tag impliqué
    }

    @Test
    void executeTask_subscribeToUser_shouldNotCreateTag() throws IOException {
        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "swawa@node1.swilabus.xyz",
                "louis@node1.swilabus.xyz",
                false
        );

        task.executeTask();

        verify(tagRepo, never()).addTag(any());
        verify(tagRepo, never()).addSubscriberToTag(any(), any());
    }

    // ===================== shouldForward =====================

    @Test
    void shouldForward_whenTrue_returnsTrue() {
        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "swawa@node1.swilabus.xyz",
                "louis@node2.swilabus.xyz",
                true
        );

        assertTrue(task.shouldForward());
    }

    @Test
    void shouldForward_whenFalse_returnsFalse() {
        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "swawa@node1.swilabus.xyz",
                "louis@node1.swilabus.xyz",
                false
        );

        assertFalse(task.shouldForward());
    }

    // ===================== IOException =====================

    @Test
    void executeTask_whenIOExceptionThrown_shouldNotPropagateException() throws IOException {
        when(tagRepo.tagExist(any())).thenReturn(false);
        doThrow(new IOException("disk error")).when(tagRepo).addTag(any());

        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "louis@node1.swilabus.xyz",
                "#monTag1@node1.swilabus.xyz",
                false
        );

        assertDoesNotThrow(task::executeTask); // ← l'exception est catchée et loggée
    }

    // ===================== getters =====================

    @Test
    void getSender_returnsCorrectSender() {
        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "louis@node1.swilabus.xyz",
                "#monTag1@node1.swilabus.xyz",
                false
        );

        assertEquals("louis@node1.swilabus.xyz", task.getSender());
    }

    @Test
    void getToSubscribe_returnsCorrectTarget() {
        SubscribeTask task = new SubscribeTask(
                userRepo, tagRepo, identityUtils,
                "louis@node1.swilabus.xyz",
                "#monTag1@node1.swilabus.xyz",
                false
        );

        assertEquals("#monTag1@node1.swilabus.xyz", task.getToSubscribe());
    }
}