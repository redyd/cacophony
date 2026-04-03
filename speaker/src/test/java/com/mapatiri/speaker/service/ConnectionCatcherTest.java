package com.mapatiri.speaker.service;

import com.google.gson.Gson;
import com.mapatiri.speaker.model.SpeakerConfig;
import com.mapatiri.speaker.repository.SpeakerConfigRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConnectionCatcherTest {

    @Test
    void listenUdpHandlesSocketSetupFailureWithoutThrowing() {
        SpeakerServer speakerServer = mock(SpeakerServer.class);
        SpeakerConfigRepository repository = mock(SpeakerConfigRepository.class);
        when(repository.getConfig()).thenReturn(configWithInvalidMulticastAddress());

        ConnectionCatcher catcher = new ConnectionCatcher(speakerServer, repository);

        assertDoesNotThrow(catcher::listenUdp);
        verifyNoInteractions(speakerServer);
    }

    private SpeakerConfig configWithInvalidMulticastAddress() {
        String json = """
                {
                  "multicastAddress": "invalid.multicast.address",
                  "multicastPort": 11003,
                  "networkInterface": "eth0",
                  "configuredDomains": []
                }
                """;
        return new Gson().fromJson(json, SpeakerConfig.class);
    }
}
