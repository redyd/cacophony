package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.factory.UDPFactory;
import com.mapatiri.node.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SpeakerAnnouncer implements Clockable {

    private final Logger logger = LoggerFactory.getLogger(SpeakerAnnouncer.class);
    private final ConfigRepository configRepository;
    private final SpeakerBridge speakerBridge;
    private final MulticastSocket multicastSocket;
    private boolean isRunning = false;

    @Inject
    public SpeakerAnnouncer(ConfigRepository jsonConfigRepository, SpeakerBridge speakerBridge, UDPFactory udpFactory) {
        this.configRepository = jsonConfigRepository;
        this.speakerBridge = speakerBridge;
        this.multicastSocket = udpFactory.create(configRepository);
    }

    @Override
    public void onClockSignal() {
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

            logger.info("ADVERT envoye");

            // Attendre la connexion TCP du speaker
            if (!isRunning) {
                new Thread(this::waitForResponse).start();
                isRunning = true;
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'envoi de l'ADVERT : {}", e.getMessage());
        }
    }

    private void waitForResponse() {
        var config = configRepository.getConfig();

        try (ServerSocket serverSocket = new ServerSocket(config.getRelayPort())) {
            logger.info("En attente de connexion du speaker sur le port {}", config.getRelayPort());

                Socket speakerSocket = serverSocket.accept();
                logger.info("Speaker connecté depuis {}", speakerSocket.getRemoteSocketAddress());
                speakerBridge.startListening(speakerSocket);
                logger.info("Speaker déconnecté, attente d'une nouvelle connexion");

        } catch (IOException e) {
            logger.error("Erreur dans waitForResponse : {}", e.getMessage());
        }
    }
}
