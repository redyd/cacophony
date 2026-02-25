package com.mapatiri.speaker.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.speaker.service.AnotherService;
import com.mapatiri.speaker.service.MyService;
import com.mapatiri.speaker.service.MyServiceImpl;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MyService.class).to(MyServiceImpl.class).in(Singleton.class); // lier une interface à une implémentation en singleton
        bind(AnotherService.class).in(Singleton.class);
    }

}
