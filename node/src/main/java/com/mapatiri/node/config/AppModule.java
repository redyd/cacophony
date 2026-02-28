package com.mapatiri.node.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.node.service.ConfigService;
import com.mapatiri.node.service.JsonService;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConfigService.class).to(JsonService.class).in(Singleton.class); // lier une interface à une implémentation en singleton
    }

}
