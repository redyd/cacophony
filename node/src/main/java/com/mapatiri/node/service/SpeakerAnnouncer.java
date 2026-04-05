package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.Advert;
import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.factory.UDPFactory;
import com.mapatiri.node.model.HeartbeatClock;
import com.mapatiri.node.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Annoncement du node via UDP. Cette classe est abonnée à {@link com.mapatiri.node.model.HeartbeatClock}.
 *
 * @author Mattéo
 */
public class SpeakerAnnouncer implements Clockable {

    private final Logger LOGGER = LoggerFactory.getLogger(SpeakerAnnouncer.class);

    private final ConfigRepository configRepository;
    private final SpeakerBridgeService speakerBridge;
    private final HeartbeatClock clock;
    private MulticastSocket multicastSocket;
    private boolean isRunning = false;

    @Inject
    public SpeakerAnnouncer(ConfigRepository jsonConfigRepository, SpeakerBridgeService speakerBridge, HeartbeatClock clock) {
        this.configRepository = jsonConfigRepository;
        this.speakerBridge = speakerBridge;
        this.clock = clock;

        this.clock.add(this);
    }

    public void init(UDPFactory factory) throws SocketCreation {
        this.multicastSocket = factory.create();
    }

    public void listenAgain() {
        this.clock.add(this);
        this.isRunning = false;
    }

    @Override
    public void onClockSignal() {
        if (multicastSocket == null) {
            throw new IllegalStateException("Multicast socket has not been initialized.");
        }

        try {
            var config = configRepository.getConfig();

            // Construire le message ADVERT
            String advert = new RegexGrammar()
                    .build(new Advert(String.valueOf(config.getRelayPort()), config.getCurrentDomain()));

            byte[] data = advert.getBytes(StandardCharsets.UTF_8);

            // Envoyer en UDP multicast
            InetAddress group = InetAddress.getByName(config.getMulticastAddress());
            DatagramPacket packet = new DatagramPacket(data, data.length, group, config.getMulticastPort());
            multicastSocket.send(packet);

            LOGGER.info("Advert has been sent.");

            // Attendre la connexion TCP du speaker
            if (!this.isRunning) {
                new Thread(this::waitForResponse).start();
                this.isRunning = true;
            }

        } catch (IOException e) {
            LOGGER.error("ErrorProtocole while sending advert: {}", e.getMessage());
        }
    }

    private void waitForResponse() {
        var config = configRepository.getConfig();

        try (ServerSocket serverSocket = new ServerSocket(config.getRelayPort())) {
            LOGGER.info("Waiting for connexion on port '{}'", config.getRelayPort());

            Socket speakerSocket = serverSocket.accept();
            LOGGER.info("Speaker connected from: '{}'", speakerSocket.getRemoteSocketAddress());
            this.clock.unsubscribe(this);
            LOGGER.info("Advert won't be sent anymore");
            speakerBridge.startListening(speakerSocket);
        } catch (IOException e) {
            LOGGER.error("ErrorProtocole while waiting for speaker connection: {}", e.getMessage());
        }
    }
}
