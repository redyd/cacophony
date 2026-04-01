package com.mapatiri.speaker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.speaker.config.AppModule;

public class App {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());

    }

}
