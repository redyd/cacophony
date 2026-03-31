package com.mapatiri.node.task;

import com.mapatiri.node.service.SpeakerBridge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepeaterTest {

    @Mock private SpeakerBridge speakerBridge;
    @Mock private SendTask sendTask;
    @Mock private SubscribeTask subscribeTask;

    private TaskRepeater taskRepeater;

    private static final String SENDER = "louis@node1.swilabus.xyz";
    private static final String CONTENT = "hello world";

    @BeforeEach
    void setUp() {
        taskRepeater = new TaskRepeater(speakerBridge);
    }

    // ===================== SendTask — distant followers =====================

    @Test
    void convertAndSend_sendTaskWithDistantFollower_forwardsPost() {
        when(sendTask.getSender()).thenReturn(SENDER);
        when(sendTask.getContent()).thenReturn(CONTENT);
        when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of("alice@node2.swilabus.xyz"));
        when(sendTask.getDistantTags()).thenReturn(Set.of());

        taskRepeater.convertAndSend(sendTask);

        verify(speakerBridge, times(1)).forward(argThat(post ->
                post.startsWith("POST") &&
                        post.contains(SENDER) &&
                        post.contains("alice@node2.swilabus.xyz")
        ));
    }

    @Test
    void convertAndSend_sendTaskWithMultipleDistantFollowers_forwardsOnePostPerFollower() {
        when(sendTask.getSender()).thenReturn(SENDER);
        when(sendTask.getContent()).thenReturn(CONTENT);
        when(sendTask.getFollowersFromOtherDomain()).thenReturn(
                Set.of("alice1@node2.swilabus.xyz", "swawa@node2.swilabus.xyz")
        );
        when(sendTask.getDistantTags()).thenReturn(Set.of());

        taskRepeater.convertAndSend(sendTask);

        verify(speakerBridge, times(2)).forward(any());
    }

    @Test
    void convertAndSend_sendTaskWithNoDistantFollowers_doesNotForward() {
        when(sendTask.getSender()).thenReturn(SENDER);
        when(sendTask.getContent()).thenReturn(CONTENT);
        when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of());
        when(sendTask.getDistantTags()).thenReturn(Set.of());

        taskRepeater.convertAndSend(sendTask);

        verifyNoInteractions(speakerBridge);
    }

    // ===================== SendTask — distant tags =====================

    @Test
    void convertAndSend_sendTaskWithDistantTag_forwardsPost() {
        when(sendTask.getSender()).thenReturn(SENDER);
        when(sendTask.getContent()).thenReturn(CONTENT);
        when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of());
        when(sendTask.getDistantTags()).thenReturn(Set.of("#monTag1@node2.swilabus.xyz"));

        taskRepeater.convertAndSend(sendTask);

        verify(speakerBridge, times(1)).forward(argThat(post ->
                post.startsWith("POST") &&
                        post.contains(SENDER) &&
                        post.contains("#monTag1@node2.swilabus.xyz")
        ));
    }

    @Test
    void convertAndSend_sendTaskWithFollowersAndTags_forwardsAll() {
        when(sendTask.getSender()).thenReturn(SENDER);
        when(sendTask.getContent()).thenReturn(CONTENT);
        when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of("alice@node2.swilabus.xyz"));
        when(sendTask.getDistantTags()).thenReturn(Set.of("#monTag1@node2.swilabus.xyz"));

        taskRepeater.convertAndSend(sendTask);

        verify(speakerBridge, times(2)).forward(any()); // 1 follower + 1 tag
    }

    // ===================== SendTask — format POST =====================

    @Test
    void convertAndSend_sendTask_postContainsMsgInnerMessage() {
        when(sendTask.getSender()).thenReturn(SENDER);
        when(sendTask.getContent()).thenReturn(CONTENT);
        when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of("alice@node2.swilabus.xyz"));
        when(sendTask.getDistantTags()).thenReturn(Set.of());

        taskRepeater.convertAndSend(sendTask);

        verify(speakerBridge).forward(argThat(post ->
                post.contains("MSG") &&
                        post.contains(CONTENT)
        ));
    }

    // ===================== SubscribeTask =====================

    @Test
    void convertAndSend_subscribeTask_forwardsPost() {
        when(subscribeTask.getSender()).thenReturn(SENDER);
        when(subscribeTask.getToSubscribe()).thenReturn("alice@node2.swilabus.xyz");

        taskRepeater.convertAndSend(subscribeTask);

        verify(speakerBridge, times(1)).forward(argThat(post ->
                post.startsWith("POST") &&
                        post.contains(SENDER) &&
                        post.contains("alice@node2.swilabus.xyz") &&
                        post.contains("SUBSCRIBE")
        ));
    }

    @Test
    void convertAndSend_subscribeTask_forwardsExactlyOnce() {
        when(subscribeTask.getSender()).thenReturn(SENDER);
        when(subscribeTask.getToSubscribe()).thenReturn("alice@node2.swilabus.xyz");

        taskRepeater.convertAndSend(subscribeTask);

        verify(speakerBridge, times(1)).forward(any());
    }

    // ===================== Unknown task =====================

    @Test
    void convertAndSend_unknownTaskType_doesNotForward() {
        ActionTask unknownTask = mock(ActionTask.class);

        taskRepeater.convertAndSend(unknownTask);

        verifyNoInteractions(speakerBridge);
    }
}