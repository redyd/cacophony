package com.mapatiri.node.task;

import com.mapatiri.node.service.SpeakerBridge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepeaterTest {

    private static final String SENDER  = "louis@node1.swilabus.xyz";
    private static final String CONTENT = "hello world";

    @Mock private SpeakerBridge speakerBridge;
    @Mock private SendTask      sendTask;
    @Mock private SubscribeTask subscribeTask;

    private TaskRepeater taskRepeater;

    @BeforeEach
    void setUp() {
        taskRepeater = new TaskRepeater(speakerBridge);
    }

    // ─────────────────────────────────────────────
    // SendTask
    // ─────────────────────────────────────────────

    @Nested
    class ConvertAndSendWithSendTask {

        @BeforeEach
        void stubBase() {
            when(sendTask.getSender()).thenReturn(SENDER);
            when(sendTask.getContent()).thenReturn(CONTENT);
        }

        @Test
        void forwardsOnePostPerDistantFollower() {
            when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of("alice@node2.swilabus.xyz"));
            when(sendTask.getDistantTags()).thenReturn(Set.of());

            taskRepeater.convertAndSend(sendTask);

            verify(speakerBridge, times(1)).forward(any());
        }

        @Test
        void forwardsOnePostPerDistantTag() {
            when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of());
            when(sendTask.getDistantTags()).thenReturn(Set.of("#monTag1@node2.swilabus.xyz"));

            taskRepeater.convertAndSend(sendTask);

            verify(speakerBridge, times(1)).forward(any());
        }

        @Test
        void forwardsFollowersAndTagsTogether() {
            when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of("alice@node2.swilabus.xyz"));
            when(sendTask.getDistantTags()).thenReturn(Set.of("#monTag1@node2.swilabus.xyz"));

            taskRepeater.convertAndSend(sendTask);

            verify(speakerBridge, times(2)).forward(any()); // 1 follower + 1 tag
        }

        @Test
        void forwardsMultipleFollowersAndMultipleTags() {
            when(sendTask.getFollowersFromOtherDomain()).thenReturn(
                    Set.of("alice@node2.swilabus.xyz", "boby1@node3.swilabus.xyz")
            );
            when(sendTask.getDistantTags()).thenReturn(
                    Set.of("#tag123@node2.swilabus.xyz", "#tag234@node3.swilabus.xyz")
            );

            taskRepeater.convertAndSend(sendTask);

            verify(speakerBridge, times(4)).forward(any());
        }

        @Test
        void doesNotForwardWhenNoFollowersAndNoTags() {
            when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of());
            when(sendTask.getDistantTags()).thenReturn(Set.of());

            taskRepeater.convertAndSend(sendTask);

            verify(speakerBridge, never()).forward(any());
        }

        @Test
        void forwardedPostContainsSenderAndContent() {
            when(sendTask.getFollowersFromOtherDomain()).thenReturn(Set.of("alice@node2.swilabus.xyz"));
            when(sendTask.getDistantTags()).thenReturn(Set.of());

            taskRepeater.convertAndSend(sendTask);

            verify(speakerBridge).forward(argThat(post ->
                    post.contains(SENDER) && post.contains(CONTENT)
            ));
        }
    }

    // ─────────────────────────────────────────────
    // SubscribeTask
    // ─────────────────────────────────────────────

    @Nested
    class ConvertAndSendWithSubscribeTask {

        @BeforeEach
        void stubBase() {
            when(subscribeTask.getSender()).thenReturn(SENDER);
            when(subscribeTask.getToSubscribe()).thenReturn("alice@node2.swilabus.xyz");
        }

        @Test
        void forwardsExactlyOnePost() {
            taskRepeater.convertAndSend(subscribeTask);

            verify(speakerBridge, times(1)).forward(any());
        }

        @Test
        void forwardedPostContainsSenderAndTarget() {
            taskRepeater.convertAndSend(subscribeTask);

            verify(speakerBridge).forward(argThat(post ->
                    post.contains(SENDER) && post.contains("alice@node2.swilabus.xyz")
            ));
        }

        @Test
        void forwardsSubscribeToTag() {
            when(subscribeTask.getToSubscribe()).thenReturn("#tag12@node2.swilabus.xyz");

            taskRepeater.convertAndSend(subscribeTask);

            verify(speakerBridge, times(1)).forward(argThat(post ->
                    post.contains("#tag12@node2.swilabus.xyz")
            ));
        }
    }

    // ─────────────────────────────────────────────
    // Unknown task type
    // ─────────────────────────────────────────────

    @Nested
    class ConvertAndSendWithUnknownTask {

        @Test
        void doesNotForwardForUnknownTaskType() {
            ActionTask unknownTask = mock(ActionTask.class);

            taskRepeater.convertAndSend(unknownTask);

            verify(speakerBridge, never()).forward(any());
        }
    }
}