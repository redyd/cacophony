package com.mapatiri.speaker.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.speaker.repository.JsonSpeakerRepository;
import com.mapatiri.speaker.repository.SpeakerConfigRepository;
import com.mapatiri.speaker.service.AESEncrypter;
import com.mapatiri.speaker.service.ConnectionCatcher;
import com.mapatiri.speaker.service.NodeStore;
import com.mapatiri.speaker.service.SpeakerServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppModuleTest {

    @Test
    void bindsRepositoryAsSingletonToJsonImplementation() {
        Injector injector = Guice.createInjector(new AppModule());

        SpeakerConfigRepository repository1 = injector.getInstance(SpeakerConfigRepository.class);
        SpeakerConfigRepository repository2 = injector.getInstance(SpeakerConfigRepository.class);

        assertTrue(repository1 instanceof JsonSpeakerRepository);
        assertSame(repository1, repository2);
    }

    @Test
    void bindsServicesAsSingletons() {
        Injector injector = Guice.createInjector(new AppModule());

        NodeStore nodeStore1 = injector.getInstance(NodeStore.class);
        NodeStore nodeStore2 = injector.getInstance(NodeStore.class);
        AESEncrypter aesEncrypter1 = injector.getInstance(AESEncrypter.class);
        AESEncrypter aesEncrypter2 = injector.getInstance(AESEncrypter.class);
        SpeakerServer speakerServer1 = injector.getInstance(SpeakerServer.class);
        SpeakerServer speakerServer2 = injector.getInstance(SpeakerServer.class);
        ConnectionCatcher connectionCatcher1 = injector.getInstance(ConnectionCatcher.class);
        ConnectionCatcher connectionCatcher2 = injector.getInstance(ConnectionCatcher.class);

        assertSame(nodeStore1, nodeStore2);
        assertSame(aesEncrypter1, aesEncrypter2);
        assertSame(speakerServer1, speakerServer2);
        assertSame(connectionCatcher1, connectionCatcher2);
    }
}
