package com.mapatiri.node.model;

import com.mapatiri.node.service.Clockable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class HeartbeatClock implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatClock.class);
    private static final long TICK_INTERVAL_MS = 30_000L;

    private final Set<Clockable> clockables = new CopyOnWriteArraySet<>();

    public void add(Clockable clockable) {
        clockables.add(clockable);
    }

    @Override
    public void run() {
        logger.info("L'horloge démarre — envoi d'un tick toutes les 30s");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(TICK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Horloge interrompue.");
                return;
            }

            logger.debug("Tick d'horloge");
            for (Clockable clockable : clockables) {
                try {
                    clockable.onClockSignal();
                } catch (Exception e) {
                    logger.warn("Erreur lors de l'envoi du signal pour {}: {}", clockable.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }
}
