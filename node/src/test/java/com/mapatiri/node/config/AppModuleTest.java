package com.mapatiri.node.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.node.controller.Dispatcher;
import com.mapatiri.node.controller.TaskDispatcher;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.JsonConfigRepository;
import com.mapatiri.node.repository.JsonTagRepository;
import com.mapatiri.node.repository.JsonUserRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.SpeakerBridge;
import com.mapatiri.node.service.SpeakerBridgeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppModuleTest {

    @Test
    void bindsInterfacesToImplementations() {
        Injector injector = Guice.createInjector(new AppModule());

        assertTrue(injector.getInstance(Dispatcher.class) instanceof TaskDispatcher);
        assertTrue(injector.getInstance(SpeakerBridgeService.class) instanceof SpeakerBridge);
        assertTrue(injector.getInstance(ConfigRepository.class) instanceof JsonConfigRepository);
        assertTrue(injector.getInstance(UserRepo.class) instanceof JsonUserRepository);
        assertTrue(injector.getInstance(TagRepo.class) instanceof JsonTagRepository);
    }

    @Test
    void bindsConnectionStoreAsSingleton() {
        Injector injector = Guice.createInjector(new AppModule());

        ConnectionStore store1 = injector.getInstance(ConnectionStore.class);
        ConnectionStore store2 = injector.getInstance(ConnectionStore.class);

        assertSame(store1, store2);
    }
}
