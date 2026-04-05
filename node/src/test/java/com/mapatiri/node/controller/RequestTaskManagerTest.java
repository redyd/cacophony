package com.mapatiri.node.controller;

import com.mapatiri.node.task.ActionTask;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestTaskManagerTest {

    @Test
    void submitExecutesTaskAndForwardsWhenRequested() throws Exception {
        TaskRepeater repeater = mock(TaskRepeater.class);
        ActionTask task = mock(ActionTask.class);
        CountDownLatch latch = new CountDownLatch(1);

        when(task.shouldForward()).thenReturn(true);
        org.mockito.Mockito.doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(task).executeTask();

        RequestTaskManager manager = new RequestTaskManager(repeater);
        manager.submit(task);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(task).executeTask();
        verify(repeater, timeout(2000)).convertAndSend(task);
    }

    @Test
    void submitExecutesTaskWithoutForwardingWhenNotRequested() throws Exception {
        TaskRepeater repeater = mock(TaskRepeater.class);
        ActionTask task = mock(ActionTask.class);
        CountDownLatch latch = new CountDownLatch(1);

        when(task.shouldForward()).thenReturn(false);
        org.mockito.Mockito.doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(task).executeTask();

        RequestTaskManager manager = new RequestTaskManager(repeater);
        manager.submit(task);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(task).executeTask();
        org.mockito.Mockito.verifyNoInteractions(repeater);
    }
}
