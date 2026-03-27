package com.mapatiri.node.service;

import com.google.inject.Provider;
import com.mapatiri.node.task.TaskConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeakerBridgeTest {

    @Mock private AESEncrypter aes;
    @Mock private TaskConverter taskConverter;
    @Mock private Provider<TaskConverter> taskConverterProvider;

    @InjectMocks
    private SpeakerBridge speakerBridge;

    // ===== forward() =====

    @Test
    void forward_beforeListening_doesNotThrow() {
        // out est null → doit juste logger et ne pas crasher
        assertDoesNotThrow(() -> speakerBridge.forward("POST something"));
    }

    @Test
    void forward_afterListening_encryptsAndSends() throws Exception {
        // Simuler un socket pour initialiser out
        Socket socket = buildSocket(""); // stream vide
        when(aes.encrypt("POST hello")).thenReturn("ENCRYPTED");

        speakerBridge.startListening(socket); // initialise out
        speakerBridge.forward("POST hello");

        verify(aes).encrypt("POST hello");
    }

    // ===== startListening() =====

    @Test
    void startListening_postMessage_callsTaskConverter() throws Exception {
        when(taskConverterProvider.get()).thenReturn(taskConverter);
        String encryptedLine = "ENCRYPTED_POST";
        Socket socket = buildSocket(encryptedLine + "\n");

        when(aes.decrypt(encryptedLine)).thenReturn("POST 123@domainA alice1@domainA bob12@domainB MSG bob12@domainB hello_world");

        speakerBridge.startListening(socket);

        verify(taskConverter).convertAndSend(contains("POST"));
    }

    @Test
    void startListening_unknownMessage_doesNotCallTaskConverter() throws Exception {
        String encryptedLine = "ENCRYPTED_UNKNOWN";
        Socket socket = buildSocket(encryptedLine + "\n");

        when(aes.decrypt(encryptedLine)).thenReturn("UNKNOWN something");

        speakerBridge.startListening(socket);

        verify(taskConverter, never()).convertAndSend(any());
    }

    @Test
    void startListening_emptyStream_doesNotCallTaskConverter() throws Exception {
        Socket socket = buildSocket(""); // readLine retourne null immédiatement

        speakerBridge.startListening(socket);

        verify(taskConverter, never()).convertAndSend(any());
    }

    @Test
    void startListening_ioException_doesNotThrow() throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenThrow(new IOException("socket error"));
        when(socket.getOutputStream()).thenReturn(new java.io.ByteArrayOutputStream());

        assertDoesNotThrow(() -> speakerBridge.startListening(socket));
    }

    // ===== helper =====

    private Socket buildSocket(String inputContent) throws IOException {
        Socket socket = mock(Socket.class);
        byte[] bytes = inputContent.getBytes(StandardCharsets.UTF_8);
        when(socket.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(bytes));
        when(socket.getOutputStream()).thenReturn(new java.io.ByteArrayOutputStream());
        return socket;
    }
}