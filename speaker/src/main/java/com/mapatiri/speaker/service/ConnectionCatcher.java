package com.mapatiri.speaker.service;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.speaker.model.SpeakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConnectionCatcher {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionCatcher.class);
    private final SpeakerServer speakerServer;
    private final SpeakerConfig config;

    public ConnectionCatcher(SpeakerServer speakerServer, SpeakerConfig speakerConfig) {
        this.speakerServer = speakerServer;
        this.config = speakerConfig;
    }

    /**
     * Va écouter sur le port multicast afin d'intercepter le advert pour passer les informations au speakerServer
     *
     * @throws IOException
     */
    public void listenUdp() throws IOException {
        try(MulticastSocket multicastSocket = new MulticastSocket(config.getMulticastPort())) {

            InetAddress group = InetAddress.getByName(config.getMulticastAddress());
            NetworkInterface networkInterface = NetworkInterface.getByName(config.getNetworkInterface());
            multicastSocket.joinGroup(new InetSocketAddress(group, config.getMulticastPort()), networkInterface);

            logger.info("Listening for UDP packets on port 11003");

            byte[] buffer = new byte[256];

            while (!Thread.currentThread().isInterrupted()) {
                // Attendre un paquet UDP
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                // Extraire le message et l'IP source du node
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                InetAddress nodeIP = packet.getAddress();

                logger.info("ADVERT reçu de {} : {}", nodeIP, message.trim());

                // Parser le message ADVERT
                RegexGrammar regexGrammar = new RegexGrammar(message);
                if (!regexGrammar.getTypeOfCommand().equals("ADVERT")) {
                    logger.warn("Message non ADVERT reçu, ignoré");
                    continue;
                }

                List<String> parts = regexGrammar.parseCommand();
                int relayPort = Integer.parseInt(parts.get(0));
                String domain = parts.get(1);

                // Déléguer au speakerServer pour établir la connexion TCP
                speakerServer.startConnectionFor(nodeIP, domain, relayPort);
            }
        }
    }

}
