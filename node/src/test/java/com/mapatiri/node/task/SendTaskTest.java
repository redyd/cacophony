package com.mapatiri.node.task;

import com.mapatiri.node.model.*;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendTaskTest {

    @Mock private UserRepo userRepo;
    @Mock private TagRepo tagRepo;
    @Mock private ConnectionStore connectionStore;
    @Mock private ConfigRepository configRepository;
    @Mock private NodeConfig nodeConfig;
    @Mock private User senderUser;

    private IdentityUtils identityUtils;

    private static final String DOMAIN = "node1.swilabus.xyz";
    private static final String LOGIN = "swawa";
    private static final String SENDER = LOGIN + "@" + DOMAIN;

    @BeforeEach
    void setUp() {
        identityUtils = new IdentityUtils();
        when(configRepository.getConfig()).thenReturn(nodeConfig);
        when(nodeConfig.getCurrentDomain()).thenReturn(DOMAIN);
    }

    private SendTask buildTask(List<String> tags, String content, boolean shouldForward) {
        return new SendTask(LOGIN, tags, content, shouldForward,
                connectionStore, userRepo, tagRepo, configRepository, identityUtils);
    }

    // ===================== executeTask — local followers =====================

    @Test
    void executeTask_noFollowers_sendsToNobody() {
        when(userRepo.getUserFromLogin(LOGIN)).thenReturn(senderUser);
        when(senderUser.getSubscribers()).thenReturn(new HashSet<>());
        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>()));
        when(connectionStore.getConnectionsFromLogin(anySet())).thenReturn(new HashSet<>());

        SendTask task = buildTask(List.of(), "hello", false);
        task.executeTask();

        verify(connectionStore).getConnectionsFromLogin(Collections.emptySet());
    }

    @Test
    void executeTask_withLocalFollower_sendsMessage() throws Exception {
        ClientConnection connection = mock(ClientConnection.class);

        when(userRepo.getUserFromLogin(LOGIN)).thenReturn(senderUser);
        when(senderUser.getSubscribers()).thenReturn(new HashSet<>(Set.of("louis@" + DOMAIN)));
        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>()));
        when(connectionStore.getConnectionsFromLogin(anySet())).thenReturn(Set.of(connection));

        SendTask task = buildTask(List.of(), "hello", false);
        task.executeTask();

        verify(connection).send(contains("MSG"));
    }

    @Test
    void executeTask_withTagFollower_senderSubscribedToTag_sendsMessage() throws Exception {
        ClientConnection connection = mock(ClientConnection.class); // ← déplacé ici en premier

        Tag tag = new Tag("#monTag1", DOMAIN);
        tag.addSubscriber("louis@" + DOMAIN);
        tag.addSubscriber(SENDER);

        when(userRepo.getUserFromLogin(LOGIN)).thenReturn(senderUser);
        when(senderUser.getSubscribers()).thenReturn(new HashSet<>());
        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>(List.of(tag))));
        when(connectionStore.getConnectionsFromLogin(anySet())).thenReturn(Set.of(connection));

        SendTask task = buildTask(List.of("#monTag1"), "hello #monTag1", false);
        task.executeTask();

        verify(connection).send(contains("MSG"));
    }

    @Test
    void executeTask_withTagFollower_senderNotSubscribedToTag_doesNotSend() throws Exception {
        Tag tag = new Tag("#monTag1", DOMAIN);
        tag.addSubscriber("louis@" + DOMAIN);
        // sender n'est PAS abonné au tag

        when(userRepo.getUserFromLogin(LOGIN)).thenReturn(senderUser);
        when(senderUser.getSubscribers()).thenReturn(new HashSet<>());
        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>(List.of(tag))));
        when(connectionStore.getConnectionsFromLogin(anySet())).thenReturn(new HashSet<>());

        SendTask task = buildTask(List.of("#monTag1"), "hello #monTag1", false);
        task.executeTask();

        verify(connectionStore).getConnectionsFromLogin(Collections.emptySet());
    }

    // ===================== shouldForward =====================

    @Test
    void shouldForward_whenTrue_returnsTrue() {
        SendTask task = buildTask(List.of(), "hello", true);
        assertTrue(task.shouldForward());
    }

    @Test
    void shouldForward_whenFalse_returnsFalse() {
        SendTask task = buildTask(List.of(), "hello", false);
        assertFalse(task.shouldForward());
    }

    // ===================== getFollowersFromOtherDomain =====================

    @Test
    void getFollowersFromOtherDomain_returnsDistantSubscribers() {
        String distantUser = "alice@node2.swilabus.xyz";

        when(userRepo.getUserFromLogin(LOGIN)).thenReturn(senderUser);
        when(senderUser.getSubscribers()).thenReturn(new HashSet<>(Set.of(distantUser)));
        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>()));

        SendTask task = buildTask(List.of(), "hello", true);
        Set<String> result = task.getFollowersFromOtherDomain();

        assertTrue(result.contains(distantUser));
    }

    @Test
    void getFollowersFromOtherDomain_doesNotReturnLocalSubscribers() {
        String localUser = "louis@" + DOMAIN;

        when(userRepo.getUserFromLogin(LOGIN)).thenReturn(senderUser);
        when(senderUser.getSubscribers()).thenReturn(new HashSet<>(Set.of(localUser)));
        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>()));

        SendTask task = buildTask(List.of(), "hello", true);
        Set<String> result = task.getFollowersFromOtherDomain();

        assertFalse(result.contains(localUser));
    }

    // ===================== getDistantTags =====================

    @Test
    void getDistantTags_returnsTagsFromOtherDomain() {
        Tag distantTag = new Tag("#monTag1", "node2.swilabus.xyz");
        distantTag.addSubscriber(SENDER); // sender est abonné

        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>(List.of(distantTag))));

        SendTask task = buildTask(List.of("#monTag1"), "hello #monTag1", true);
        Set<String> result = task.getDistantTags();

        assertTrue(result.contains("#monTag1@node2.swilabus.xyz"));
    }

    @Test
    void getDistantTags_doesNotReturnLocalTags() {
        Tag localTag = new Tag("#monTag1", DOMAIN); // même domaine
        localTag.addSubscriber(SENDER);

        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>(List.of(localTag))));

        SendTask task = buildTask(List.of("#monTag1"), "hello #monTag1", true);
        Set<String> result = task.getDistantTags();

        assertTrue(result.isEmpty());
    }

    @Test
    void getDistantTags_senderNotSubscribed_returnsEmpty() {
        Tag distantTag = new Tag("#monTag1", "node2.swilabus.xyz");
        // sender n'est PAS dans les subscribers

        when(tagRepo.getAllTags()).thenReturn(new Tags(new ArrayList<>(List.of(distantTag))));

        SendTask task = buildTask(List.of("#monTag1"), "hello #monTag1", true);
        Set<String> result = task.getDistantTags();

        assertTrue(result.isEmpty());
    }

    // ===================== getters =====================

    @Test
    void getSender_returnsFullAddress() {
        SendTask task = buildTask(List.of(), "hello", false);
        assertEquals(SENDER, task.getSender());
    }

    @Test
    void getContent_returnsContent() {
        SendTask task = buildTask(List.of(), "hello world", false);
        assertEquals("hello world", task.getContent());
    }
}