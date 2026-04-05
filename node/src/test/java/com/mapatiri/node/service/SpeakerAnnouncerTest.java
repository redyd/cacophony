package com.mapatiri.node.service;

import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.factory.UDPFactory;
import com.mapatiri.node.model.HeartbeatClock;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeakerAnnouncerTest {

    @Mock private ConfigRepository configRepository;
    @Mock private SpeakerBridgeService speakerBridge;
    @Mock private HeartbeatClock clock;
    @Mock private UDPFactory udpFactory;
    @Mock private MulticastSocket multicastSocket;
    @Mock private NodeConfig nodeConfig;
    private int freePort;

    private SpeakerAnnouncer speakerAnnouncer;

    @BeforeEach
    void setUp() throws IOException, SocketCreation {
        try (ServerSocket tmp = new ServerSocket(0)) {
            freePort = tmp.getLocalPort();
        }

        when(udpFactory.create()).thenReturn(multicastSocket);
        when(configRepository.getConfig()).thenReturn(nodeConfig);
        when(nodeConfig.getRelayPort()).thenReturn(freePort);
        when(nodeConfig.getCurrentDomain()).thenReturn("node1.swilabus.xyz");
        when(nodeConfig.getMulticastAddress()).thenReturn("224.224.200.200");
        when(nodeConfig.getMulticastPort()).thenReturn(11003);

        speakerAnnouncer = new SpeakerAnnouncer(configRepository, speakerBridge, clock);
        speakerAnnouncer.init(udpFactory);
    }

    @Test
    void should_send_advert_packet() throws Exception {
        speakerAnnouncer.onClockSignal();

        verify(multicastSocket).send(any(DatagramPacket.class));
    }

    @Test
    void onClockSignal_startsWaitForResponseThreadOnce() throws Exception {
        speakerAnnouncer.onClockSignal();
        speakerAnnouncer.onClockSignal();

        verify(multicastSocket, times(2)).send(any(DatagramPacket.class));
    }

    @Test
    void onClockSignal_ioException_doesNotThrow() throws Exception {
        doThrow(new IOException("network error")).when(multicastSocket).send(any());

        assertDoesNotThrow(() -> speakerAnnouncer.onClockSignal());
    }

    @Test
    void onClockSignal_speakerConnects_startListeningCalled() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(speakerBridge).startListening(any(Socket.class));

        speakerAnnouncer.onClockSignal();

        Thread.sleep(200);

        try (Socket client = new Socket("localhost", freePort);) {
            assertTrue(latch.await(3, TimeUnit.SECONDS));
        }

        verify(speakerBridge).startListening(any(Socket.class));
    }
}
