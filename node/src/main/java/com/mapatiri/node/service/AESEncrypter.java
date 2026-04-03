package com.mapatiri.node.service;

import com.google.inject.Inject;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.JsonConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Créé à l'aide de l'IA
 * Permet d'encrypter et de décrypter des messages.
 *
 * @author Mattéo
 */
public class AESEncrypter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AESEncrypter.class);

    private final ConfigRepository jsonConfigRepository;

    @Inject
    public AESEncrypter(ConfigRepository jsonConfigRepository) {
        this.jsonConfigRepository = jsonConfigRepository;
    }

    private SecretKey getSecretKey() {
        String key = jsonConfigRepository.getConfig().getBase64AES();
        byte[] keyBytes = Base64.getDecoder().decode(key);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String content) {
        try {
            // Génération de l'IV afin d'avoir a chaque chiffrage, un message différent
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);

            // Création du cipher qui va permettre de chiffrer le message
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);

            // Chiffrage du contenu
            byte[] cipherText = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(spec.getIV())
                    + ":"
                    + Base64.getEncoder().encodeToString(cipherText);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            LOGGER.error("Could not encrypt content: {}", e.getMessage());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public String decrypt(String content) {
        try {
            // Séparer l'IV et le ciphertext
            String[] parts = content.split(":", 2);

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);

            // Recréer l'IvParameterSpec depuis l'IV récupéré
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);

            // Créer le cipher en mode déchiffrement
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);

            // Déchiffrer et retourner le message en clair
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("Could not decrypt content: {}", e.getMessage());
        }
        return "";
    }
}
