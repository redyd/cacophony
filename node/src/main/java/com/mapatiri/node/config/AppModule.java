package com.mapatiri.node.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.node.factory.TLSContextFactory;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.JsonConfigRepository;
import com.mapatiri.node.server.TLSServer;
import com.mapatiri.node.service.*;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind interface to implementation
        bind(ConfigRepository.class).to(JsonConfigRepository.class).in(Singleton.class);
        bind(Dispatcher.class).to(FakeDispatcher.class).in(Singleton.class);

        // Define singleton services
        bind(ConnectionStore.class).in(Singleton.class);
        bind(TLSContextFactory.class).in(Singleton.class);
        bind(TLSServer.class).in(Singleton.class);
    }

}
