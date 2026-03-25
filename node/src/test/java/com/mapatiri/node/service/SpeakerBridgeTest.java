package com.mapatiri.node.service;

import com.mapatiri.node.task.TaskConverter;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;

import static org.mockito.Mockito.*;

class SpeakerBridgeTest {
    @Test
    void should_forward_when_encrypted() throws Exception {

        AESEncrypter aes = mock(AESEncrypter.class);
        TaskConverter taskConverter = mock(TaskConverter.class);

        when(aes.encrypt("hello")).thenReturn("encryptedMessage");

        SpeakerBridge bridge = new SpeakerBridge(aes, taskConverter);

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer, true);

        // injecter le PrintWriter dans la classe
        java.lang.reflect.Field field = SpeakerBridge.class.getDeclaredField("out");
        field.setAccessible(true);
        field.set(bridge, out);

        bridge.forward("hello");

        assert writer.toString().contains("encryptedMessage");
    }

    @Test
    void should_start_listening_when_connection_is_accept() throws Exception {

        AESEncrypter aes = mock(AESEncrypter.class);
        TaskConverter taskConverter = mock(TaskConverter.class);

        when(aes.decrypt("encrypted")).thenReturn("POST something");

        String input = "encrypted\n";

        Socket socket = mock(Socket.class);

        when(socket.getInputStream()).thenReturn(
                new java.io.ByteArrayInputStream(input.getBytes())
        );

        when(socket.getOutputStream()).thenReturn(
                new java.io.ByteArrayOutputStream()
        );

        SpeakerBridge bridge = new SpeakerBridge(aes, taskConverter);

        Thread thread = new Thread(() -> bridge.startListening(socket));
        thread.start();

        Thread.sleep(200);

        verify(aes).decrypt("encrypted");
    }
}