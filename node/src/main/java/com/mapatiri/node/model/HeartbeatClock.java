package com.mapatiri.node.model;

import com.mapatiri.node.service.Clockable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class HeartbeatClock implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatClock.class);
    private static final long TICK_INTERVAL_MS = 30_000L;

    private final Set<Clockable> clockables = new HashSet<>();

    public void add(Clockable clockable) {
        clockables.add(clockable);
    }

    @Override
    public void run() {
        logger.info("Clock set to {} seconds and started", TICK_INTERVAL_MS / 1000);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(TICK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Clock has been interrupted");
                return;
            }

            logger.info("Ding dong !");
            for (Clockable clockable : clockables) {
                try {
                    clockable.onClockSignal();
                } catch (Exception e) {
                    logger.warn("Could not wake up {} because of: {}", clockable.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }
}
