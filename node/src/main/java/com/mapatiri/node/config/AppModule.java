package com.mapatiri.node.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.node.factory.TLSContextFactory;
import com.mapatiri.node.service.ConfigService;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.JsonService;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConfigService.class).to(JsonService.class).in(Singleton.class); // bind interface <-> implementation
        bind(ConnectionStore.class).in(Singleton.class);
        bind(TLSContextFactory.class).in(Singleton.class);
    }

}
