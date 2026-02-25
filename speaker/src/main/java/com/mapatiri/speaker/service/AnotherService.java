package com.mapatiri.speaker.service;

import com.google.inject.Inject;

public class AnotherService {
    private final MyService myService;

    @Inject
    public AnotherService(MyService myService) {
        this.myService = myService;
    }

    public void doSomething() {
        myService.doSomething();
    }

}
