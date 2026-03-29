package com.mapatiri.node.service;

import com.google.inject.Provider;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.task.TaskConverter;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Bridge entre le node et le speaker qui permet de faire transiter des informations, tant bien en réception qu'en émission.
 * Cette classe encrypte et décrypte ses communications.
 *
 * @author Mattéo
 */
public class SpeakerBridge implements SpeakerBridgeService {

    private final Logger LOGGER = LoggerFactory.getLogger(SpeakerBridge.class);

    private final AESEncrypter aes;
    private final Provider<TaskConverter> taskConverterProvider;

    private PrintWriter out;

    @Inject
    public SpeakerBridge(AESEncrypter aes, Provider<TaskConverter> taskConverterProvider) {
        this.aes = aes;
        this.taskConverterProvider = taskConverterProvider;
    }

    /**
     * Chiffre et transmet un message POST au speaker via la connexion TCP établie.
     * Utilisé par le node pour relayer un noise vers un domaine distant.
     *
     * @param message le message a crypter puis a envoyer
     */
    @Override
    public void forward(String message) {
        if (out == null) {
            LOGGER.info("Could not forward: the output is null");
            return;
        }

        try {
            String encrypted = aes.encrypt(message);
            out.println(encrypted);
            LOGGER.info("A crypted message has been sent");
        } catch (Exception ex) {
            LOGGER.error("A message could not been send", ex);
        }
    }

    /**
     * Démarre l'écoute des messages en provenance du speaker sur la connexion TCP établie.
     * Déchiffre chaque message reçu avec AES et le transmet au TaskConverter pour traitement.
     * Tourne indéfiniment jusqu'à la perte de connexion avec le speaker.
     */
    @Override
    public void startListening(Socket speakerSocket) {
        try {
            // Préparer les flux
            this.out = new PrintWriter(speakerSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(speakerSocket.getInputStream(), StandardCharsets.UTF_8)
            );

            LOGGER.info("Speaker connection is active on '{}'", speakerSocket.getRemoteSocketAddress());

            String encryptedLine;
            while ((encryptedLine = in.readLine()) != null) {
                // Déchiffrer le message reçu
                String decrypted = aes.decrypt(encryptedLine);
                LOGGER.info("A message has been received and decrypt from speaker");

                // Identifier et valider le message
                RegexGrammar grammar = new RegexGrammar(decrypted);
                String type = grammar.getTypeOfCommand();

                if (type.equals("POST")) {
                    taskConverterProvider.get().convertAndSend(decrypted);
                } else {
                    LOGGER.warn("An unknown message type has been received from speaker: '{}'", type.isBlank() ? "inconnu" : type);
                }
            }
            LOGGER.warn("Connection has been lost with speaker '{}'", speakerSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            LOGGER.error("Error while listening speaker messages: {}", e.getMessage());
        }
    }
}
