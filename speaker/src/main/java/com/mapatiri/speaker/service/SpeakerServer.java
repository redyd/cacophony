package com.mapatiri.speaker.service;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.Post;
import com.mapatiri.speaker.model.NodeConnection;
import com.mapatiri.speaker.repository.SpeakerConfigRepository;
import jakarta.inject.Inject;
import org.checkerframework.checker.regex.qual.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class SpeakerServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpeakerServer.class);

    private final RegexGrammar regexGrammar = new RegexGrammar();
    private final NodeStore nodeStore;
    private final AESEncrypter aesEncrypter;
    private final SpeakerConfigRepository configRepository;

    @Inject
    public SpeakerServer(NodeStore nodeStore, AESEncrypter aesEncrypter, SpeakerConfigRepository configRepository) {
        this.nodeStore = nodeStore;
        this.aesEncrypter = aesEncrypter;
        this.configRepository = configRepository;
    }

    public void startConnectionFor(InetAddress nodeIp, String domain, int port) {
        boolean isKnown = configRepository.getConfig()
                .getConfiguredDomains()
                .stream()
                .anyMatch(d -> d.getDomain().equals(domain));

        if (!isKnown) {
            LOGGER.warn("Unknown domain ignored : {}", domain);
            return;
        }

        if (nodeStore.isConnected(domain)) {
            LOGGER.info("Already connected to {}, ADVERT ignored", domain);
            return;
        }

        new Thread(() -> handleNode(nodeIp, domain, port)).start();
    }

    private void handleNode(InetAddress nodeIp, String domain, int port) {
        try {
            Socket socket = new Socket(nodeIp, port);
            NodeConnection nodeConnection = new NodeConnection(domain, socket);
            nodeStore.Add(nodeConnection);

            LOGGER.info("Connected to node {}", domain);

            RegexGrammar grammar = new RegexGrammar();
            String encryptedLine;
            while ((encryptedLine = nodeConnection.waitForMessage()) != null) {
                String decrypted = aesEncrypter.decrypt(domain, encryptedLine);

                try {
                    // Parser le POST pour trouver le domaine destinataire
                    switch (grammar.parse(decrypted)) {
                        case Post p -> {
                            // Extraire le domaine du destinataire
                            String destDomain = p.receiver().substring(p.receiver().indexOf('@') + 1);
                            // Rechiffrer avec la clé du node destinataire et envoyer
                            String reEncrypted = aesEncrypter.encrypt(destDomain, decrypted);
                            nodeStore.broadcast(destDomain, reEncrypted);
                        }
                        default -> LOGGER.warn("Expected POST command from node {}, ignoring: {}", domain, decrypted.trim());
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Unrecognized or malformed message from node {}, ignoring: {}", domain, decrypted.trim());
                }
            }

            LOGGER.info("Node disconnected : {}", domain);
            nodeStore.remove(domain);

        } catch (IOException e) {
            LOGGER.error("Error with the node {} : {}", domain, e.getMessage());
            nodeStore.remove(domain);
        }
    }
}
