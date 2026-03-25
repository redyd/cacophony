package com.mapatiri.node.service;

import com.mapatiri.node.factory.UDPFactory;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

import static org.mockito.Mockito.*;

class SpeakerAnnouncerTest {

    private ConfigRepository repo;
    private SpeakerBridge bridge;
    private UDPFactory factory;
    private MulticastSocket socket;
    private NodeConfig config;

    @BeforeEach
    void setUp() {
        repo = mock(ConfigRepository.class);
        bridge = mock(SpeakerBridge.class);
        factory = mock(UDPFactory.class);
        socket = mock(MulticastSocket.class);
        config = mock(NodeConfig.class);
    }

    @Test
    void should_send_advert_packet() throws Exception {

        when(repo.getConfig()).thenReturn(config);
        when(factory.create(repo)).thenReturn(socket);

        when(config.getRelayPort()).thenReturn(5003);
        when(config.getMulticastPort()).thenReturn(11003);
        when(config.getMulticastAddress()).thenReturn("224.224.200.200");
        when(config.getCurrentDomain()).thenReturn("node1.swilabus.xyz");

        SpeakerAnnouncer announcer = new SpeakerAnnouncer(repo, bridge, factory);

        announcer.onClockSignal();

        verify(socket).send(any(DatagramPacket.class));
    }
}