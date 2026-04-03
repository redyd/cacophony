package com.mapatiri.node.service;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthTaskManagerTest {

    @Test
    void startHandshakeUsesLoginTaskOnUserAction() throws Exception {
        LoginTask loginTask = mock(LoginTask.class);
        RegisterTask registerTask = mock(RegisterTask.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        NodeConfig nodeConfig = mock(NodeConfig.class);
        Socket socket = mock(Socket.class);
        ClientConnection connection = mock(ClientConnection.class);

        when(configRepository.getConfig()).thenReturn(nodeConfig);
        when(nodeConfig.getCurrentDomain()).thenReturn("node1.swilabus.xyz");
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("USER alice11\r\n".getBytes(StandardCharsets.UTF_8)));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(loginTask.authenticate(any(), any(), any(), any(), anyString())).thenReturn(connection);

        AuthTaskManager manager = new AuthTaskManager(loginTask, registerTask, configRepository);

        ClientConnection result = manager.startHandshake(socket);

        assertNotNull(result);
    }

    @Test
    void startHandshakeUsesRegisterTaskOnOnboarding() throws Exception {
        LoginTask loginTask = mock(LoginTask.class);
        RegisterTask registerTask = mock(RegisterTask.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        NodeConfig nodeConfig = mock(NodeConfig.class);
        Socket socket = mock(Socket.class);
        ClientConnection connection = mock(ClientConnection.class);

        when(configRepository.getConfig()).thenReturn(nodeConfig);
        when(nodeConfig.getCurrentDomain()).thenReturn("node1.swilabus.xyz");
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("ONBOARDING alice11 10 $2b$10$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\r\n".getBytes(StandardCharsets.UTF_8)));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(registerTask.authenticate(any(), any(), any(), any(), anyString())).thenReturn(connection);

        AuthTaskManager manager = new AuthTaskManager(loginTask, registerTask, configRepository);

        ClientConnection result = manager.startHandshake(socket);

        assertNotNull(result);
    }

    @Test
    void startHandshakeThrowsForInvalidCommand() throws Exception {
        LoginTask loginTask = mock(LoginTask.class);
        RegisterTask registerTask = mock(RegisterTask.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        NodeConfig nodeConfig = mock(NodeConfig.class);
        Socket socket = mock(Socket.class);

        when(configRepository.getConfig()).thenReturn(nodeConfig);
        when(nodeConfig.getCurrentDomain()).thenReturn("node1.swilabus.xyz");
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("INVALID x\r\n".getBytes(StandardCharsets.UTF_8)));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        AuthTaskManager manager = new AuthTaskManager(loginTask, registerTask, configRepository);

        assertThrows(AuthentificationFailed.class, () -> manager.startHandshake(socket));
    }
}
