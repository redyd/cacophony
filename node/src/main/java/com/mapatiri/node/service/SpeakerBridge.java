package com.mapatiri.node.service;

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

public class SpeakerBridge {

    private final Logger logger = LoggerFactory.getLogger(SpeakerBridge.class);

    private final AESEncrypter aes;
    private final TaskConverter taskConverter;

    private PrintWriter out;

    @Inject
    public SpeakerBridge(AESEncrypter aes, TaskConverter taskConverter) {
        this.aes = aes;
        this.taskConverter = taskConverter;
    }

    /**
     * Chiffre et transmet un message POST au speaker via la connexion TCP établie.
     * Utilisé par le node pour relayer un noise vers un domaine distant.
     *
     * @param message le message a crypter puis a envoyer
     */
    public void forward(String message) {
        if(out == null) {
            logger.info("Erreur lors du forward");
            return;
        }

        try {
            String encrypted = aes.encrypt(message);
            out.println(encrypted);
            logger.info("Message forward: {}", encrypted);
        } catch (Exception ex) {
            logger.error("Erreur dans le forward du message", ex);
        }
    }

    /**
     * Démarre l'écoute des messages en provenance du speaker sur la connexion TCP établie.
     * Déchiffre chaque message reçu avec AES et le transmet au TaskConverter pour traitement.
     * Tourne indéfiniment jusqu'à la perte de connexion avec le speaker.
     */
    public void startListening(Socket speakerSocket) {
        try {
            // Préparer les flux
            this.out = new PrintWriter(speakerSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(speakerSocket.getInputStream(), StandardCharsets.UTF_8)
            );

            logger.info("SpeakerBridge démarré, en écoute depuis {}", speakerSocket.getRemoteSocketAddress());

            String encryptedLine;
            while ((encryptedLine = in.readLine()) != null) {
                // Déchiffrer le message reçu
                String decrypted = aes.decrypt(encryptedLine);
                logger.info("Message reçu du speaker : {}", decrypted);

                // Identifier et valider le message
                RegexGrammar grammar = new RegexGrammar(decrypted);
                String type = grammar.getTypeOfCommand();

                if (type.equals("POST")) {
                    taskConverter.convertAndSend(decrypted);
                } else {
                    logger.warn("Message inattendu reçu du speaker, type : {}", type.isBlank() ? "inconnu" : type);
                }
            }
            logger.warn("Connexion avec le speaker perdue depuis {}", speakerSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            logger.error("Erreur dans startListening : {}", e.getMessage());
        }
    }
}
