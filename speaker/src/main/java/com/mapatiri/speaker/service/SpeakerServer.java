package com.mapatiri.speaker.service;

import com.mapatiri.grammar.RegexGrammar;
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
    private static final Logger logger = LoggerFactory.getLogger(SpeakerServer.class);

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
            logger.warn("Domaine inconnu ignore : {}", domain);
            return;
        }

        if (nodeStore.isConnected(domain)) {
            logger.info("Deja connecte a {}, ADVERT ignore", domain);
            return;
        }

        new Thread(() -> handleNode(nodeIp, domain, port)).start();
    }

    private void handleNode(InetAddress nodeIp, String domain, int port) {
        try {
            Socket socket = new Socket(nodeIp, port);
            NodeConnection nodeConnection = new NodeConnection(domain, socket);
            nodeStore.Add(nodeConnection);

            logger.info("Connecte au node {}", domain);

            String encryptedLine;
            while ((encryptedLine = nodeConnection.waitForMessage()) != null) {
                String decrypted = aesEncrypter.decrypt(domain, encryptedLine);
                logger.debug("Message recu de {} : {}", domain, decrypted);

                // Parser le POST pour trouver le domaine destinataire
                RegexGrammar grammar = new RegexGrammar(decrypted);
                List<String> parts = grammar.parseCommand();
                String receiver = parts.get(2); // user@domain ou #tag@domain

                // Extraire le domaine du destinataire
                String destDomain = receiver.substring(receiver.indexOf('@') + 1);

                // Rechiffrer avec la clé du node destinataire et envoyer
                String reEncrypted = aesEncrypter.encrypt(destDomain, decrypted);

                nodeStore.broadcast(destDomain, reEncrypted);
            }

            logger.info("Node déconnecté : {}", domain);
            nodeStore.remove(domain);

        } catch (IOException e) {
            logger.error("Erreur avec le node {} : {}", domain, e.getMessage());
            nodeStore.remove(domain);
        }
    }
}
