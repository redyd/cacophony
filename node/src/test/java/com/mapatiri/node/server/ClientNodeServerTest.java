package com.mapatiri.node.server;

import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.controller.Dispatcher;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.exception.ServerSocketInitializer;
import com.mapatiri.node.factory.SocketFactory;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.model.User;
import com.mapatiri.node.repository.ConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ClientNodeServer Tests")
class ClientNodeServerTest {

    @Mock private SocketFactory tlsFactory;
    @Mock private Dispatcher dispatcher;
    @Mock private ConfigRepository config;
    @Mock private ServerSocket serverSocket;
    @Mock private NodeConfig nodeConfig;
    @Mock private ClientSessionEventNotifier notifier;

    @InjectMocks
    private ClientNodeServer clientNodeServer;

    // ── helpers ──────────────────────────────────────────────────────────────

    private void stubServer(int port, boolean tls) throws Exception {
        when(tlsFactory.createSocket()).thenReturn(serverSocket);
        when(serverSocket.getLocalPort()).thenReturn(port);
        when(config.getConfig()).thenReturn(nodeConfig);
        when(nodeConfig.isTls()).thenReturn(tls);
    }

    private void acceptOnceThenClose(Socket client) throws IOException {
        when(serverSocket.isClosed()).thenReturn(false).thenReturn(true);
        when(serverSocket.accept()).thenReturn(client);
    }

    // ── run() ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("run() — démarre le serveur TLS activé")
    void run_startsServer_tlsOn() throws Exception {
        Socket plain = mock(Socket.class);
        ClientConnection cc = mock(ClientConnection.class);
        stubServer(8443, true);
        acceptOnceThenClose(plain);
        when(dispatcher.startAuth(plain)).thenReturn(cc);
        when(cc.isDisconnected()).thenReturn(true);

        clientNodeServer.run();

        verify(tlsFactory).createSocket();
    }

    @Test
    @DisplayName("run() — démarre le serveur TLS désactivé")
    void run_startsServer_tlsOff() throws Exception {
        stubServer(8080, false);
        when(serverSocket.isClosed()).thenReturn(true);

        clientNodeServer.run();

        verify(tlsFactory).createSocket();
    }

    @Test
    @DisplayName("run() — ServerSocketInitializer : log erreur sans propager")
    void run_serverSocketInitializerException_logsError() throws Exception {
        when(tlsFactory.createSocket()).thenThrow(new ServerSocketInitializer("TLS init error"));

        clientNodeServer.run(); // ne doit pas lever d'exception
    }

    // ── SSLSocket handshake ───────────────────────────────────────────────────

    @Test
    @DisplayName("run() — SSLSocket : startHandshake() est appelé")
    void run_acceptsSslSocket_startsHandshake() throws Exception {
        SSLSocket ssl = mock(SSLSocket.class);
        ClientConnection cc = mock(ClientConnection.class);
        stubServer(8443, true);
        acceptOnceThenClose(ssl);

        CountDownLatch latch = new CountDownLatch(1);
        when(dispatcher.startAuth(ssl)).thenAnswer(inv -> { latch.countDown(); return cc; });
        when(cc.isDisconnected()).thenReturn(true);

        clientNodeServer.run();
        latch.await(2, TimeUnit.SECONDS);

        verify(ssl).startHandshake();
    }

    @Test
    @DisplayName("run() — Socket classique : startHandshake() n'est PAS appelé")
    void run_acceptsPlainSocket_noHandshake() throws Exception {
        Socket plain = mock(Socket.class); // volontairement pas un SSLSocket
        ClientConnection cc = mock(ClientConnection.class);
        stubServer(8080, false);
        acceptOnceThenClose(plain);

        CountDownLatch latch = new CountDownLatch(1);
        when(dispatcher.startAuth(plain)).thenAnswer(inv -> { latch.countDown(); return cc; });
        when(cc.isDisconnected()).thenReturn(true);

        clientNodeServer.run();
        latch.await(2, TimeUnit.SECONDS);

        // plain n'est pas un SSLSocket → aucune méthode de handshake ne doit exister
        verifyNoInteractions(plain);
    }

    // ── handleClient() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleClient() — message reçu → dispatcher.submit() appelé")
    void handleClient_processesMessages() throws Exception {
        Socket plain = mock(Socket.class);
        ClientConnection cc = mock(ClientConnection.class);
        User user = mock(User.class);
        stubServer(8080, false);
        acceptOnceThenClose(plain);

        when(dispatcher.startAuth(plain)).thenReturn(cc);
        when(cc.getUser()).thenReturn(user);
        when(user.getLogin()).thenReturn("alice");
        when(cc.isDisconnected()).thenReturn(false).thenReturn(true);
        when(cc.waitForMessage()).thenReturn("PING");

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> { latch.countDown(); return null; }).when(dispatcher).submit("PING", "alice");

        clientNodeServer.run();
        latch.await(2, TimeUnit.SECONDS);

        verify(dispatcher).submit("PING", "alice");
    }

    @Test
    @DisplayName("handleClient() — IOException sur waitForMessage() : warn loggué, pas de submit")
    void handleClient_ioExceptionOnWaitForMessage_logsWarn() throws Exception {
        Socket plain = mock(Socket.class);
        ClientConnection cc = mock(ClientConnection.class);
        stubServer(8080, false);
        acceptOnceThenClose(plain);

        CountDownLatch latch = new CountDownLatch(1);
        when(dispatcher.startAuth(plain)).thenAnswer(inv -> { latch.countDown(); return cc; });
        when(cc.isDisconnected()).thenReturn(false).thenReturn(true);
        when(cc.waitForMessage()).thenThrow(new IOException("socket reset"));

        clientNodeServer.run();
        latch.await(2, TimeUnit.SECONDS);

        verify(dispatcher, never()).submit(any(), any());
    }

    @Test
    @DisplayName("handleClient() — BadRequestException → client.send(\"ERROR\")")
    void handleClient_badRequestException_sendsErrorToClient() throws Exception {
        Socket plain = mock(Socket.class);
        ClientConnection cc = mock(ClientConnection.class);
        User user = mock(User.class);
        stubServer(8080, false);
        acceptOnceThenClose(plain);

        when(dispatcher.startAuth(plain)).thenReturn(cc);
        when(cc.getUser()).thenReturn(user);
        when(user.getLogin()).thenReturn("bob");
        when(cc.isDisconnected()).thenReturn(false).thenReturn(true);
        when(cc.waitForMessage()).thenReturn("BAD_MSG");
        doThrow(new BadRequestException("invalid format")).when(dispatcher).submit("BAD_MSG", "bob");

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> { latch.countDown(); return null; }).when(cc).send("ERROR");

        clientNodeServer.run();
        latch.await(2, TimeUnit.SECONDS);

        verify(cc).send(contains("ERROR"));    }

    @Test
    @DisplayName("handleClient() — AuthentificationFailed : pas de boucle message")
    void handleClient_authentificationFailed_logsInfo() throws Exception {
        Socket plain = mock(Socket.class);
        stubServer(8080, false);
        acceptOnceThenClose(plain);

        CountDownLatch latch = new CountDownLatch(1);
        // On utilise doAnswer pour lever l'exception ET décrémenter le latch
        doAnswer(inv -> {
            latch.countDown();
            throw new AuthentificationFailed("wrong credentials");
        }).when(dispatcher).startAuth(plain);

        clientNodeServer.run();
        latch.await(2, TimeUnit.SECONDS);

        verify(dispatcher).startAuth(plain);
        verify(dispatcher, never()).submit(any(), any());
    }
}