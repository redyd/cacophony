package com.mapatiri.node;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.node.config.AppModule;
import com.mapatiri.node.service.ConfigService;

public class App {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        var jsonService = injector.getInstance(ConfigService.class);
    }

}
