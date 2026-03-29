package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.factory.UDPFactory;
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
    private final SpeakerBridge speakerBridge;
    private MulticastSocket multicastSocket;
    private boolean isRunning = false;

    @Inject
    public SpeakerAnnouncer(ConfigRepository jsonConfigRepository, SpeakerBridge speakerBridge) {
        this.configRepository = jsonConfigRepository;
        this.speakerBridge = speakerBridge;
    }

    public void init(UDPFactory factory) throws SocketCreation {
        this.multicastSocket = factory.create();
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
                    .buildCommand("ADVERT", String.valueOf(config.getRelayPort()), config.getCurrentDomain());

            byte[] data = advert.getBytes(StandardCharsets.UTF_8);

            // Envoyer en UDP multicast
            InetAddress group = InetAddress.getByName(config.getMulticastAddress());
            DatagramPacket packet = new DatagramPacket(data, data.length, group, config.getMulticastPort());
            multicastSocket.send(packet);

            LOGGER.info("Advert has been sent.");

            // Attendre la connexion TCP du speaker
            if (!isRunning) {
                new Thread(this::waitForResponse).start();
                isRunning = true;
            }

        } catch (IOException e) {
            LOGGER.error("Error while sending advert: {}", e.getMessage());
        }
    }

    private void waitForResponse() {
        var config = configRepository.getConfig();

        try (ServerSocket serverSocket = new ServerSocket(config.getRelayPort())) {
            LOGGER.info("Waiting connexion on port '{}'", config.getRelayPort());

            Socket speakerSocket = serverSocket.accept();
            LOGGER.info("Speaker connected from: '{}'", speakerSocket.getRemoteSocketAddress());
            speakerBridge.startListening(speakerSocket);
        } catch (IOException e) {
            LOGGER.error("Error while waiting for speaker connection: {}", e.getMessage());
        }
    }
}
