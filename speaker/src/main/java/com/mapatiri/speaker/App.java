package com.mapatiri.speaker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mapatiri.speaker.config.AppModule;
import com.mapatiri.speaker.service.MyService;

public class App {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        MyService myService = injector.getInstance(MyService.class);
        myService.doSomething();
    }

}
