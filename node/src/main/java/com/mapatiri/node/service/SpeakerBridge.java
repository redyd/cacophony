package com.mapatiri.node.service;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.repository.JsonConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.io.PrintWriter;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Handler;

public class SpeakerBridge {

    private final Logger logger = LoggerFactory.getLogger(SpeakerBridge.class);

    private final AESEncrypter aes;
    private final RegexGrammar regexGrammar;

    private PrintWriter out;

    public SpeakerBridge(AESEncrypter aes, RegexGrammar regexGrammar) {
        this.aes = aes;
        this.regexGrammar = regexGrammar;
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

    public void startListening(SocketAddress socketAddress) {

    }
}
