package com.mapatiri.node.service;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.JsonConfigRepository;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Handler;

public class SpeakerBridge {

    private final Logger logger = LoggerFactory.getLogger(SpeakerBridge.class);

    private final AESEncrypter aes;
    private final RegexGrammar regexGrammar;
    private final ConfigRepository configRepository;

    private PrintWriter out;

    @Inject
    public SpeakerBridge(AESEncrypter aes, ConfigRepository configRepository) {
        this.aes = aes;
        this.configRepository = configRepository;
        this.regexGrammar = new RegexGrammar();
    }

    public void forward(String message) throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            String encrypted = aes.encrypt(message);
            out.println(encrypted);
            logger.info("Messager forward: {}", encrypted);
        } catch (Exception ex) {
            logger.error("Erreur dans le forward du message", ex);
        }
    }

    public void startListening(Socket speakerSocket) throws IOException {
        NodeConfig config = configRepository.getConfig();
        this.out = new PrintWriter(speakerSocket.getOutputStream(), true);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(speakerSocket.getInputStream(), StandardCharsets.UTF_8)
        );

        if(config.getBase64AES() == null) {
            logger.info("Le node ne possede pas de cle AES");
        }

        String encryptedLine;
        while ((encryptedLine = in.readLine()) != null) {
            // Déchiffrer le message reçu
            String decrypted = aes.decrypt(encryptedLine);
            logger.debug("Message reçu du speaker : {}", decrypted);

            // Valider avec la grammar et passer au TaskHandler

        }
    }
}
