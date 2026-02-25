package com.mapatiri.speaker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyServiceImpl implements MyService {

    private static final Logger logger = LoggerFactory.getLogger(MyServiceImpl.class);

    @Override
    public void doSomething() {
        logger.info("MyServiceImpl in speaker");
    }
}
