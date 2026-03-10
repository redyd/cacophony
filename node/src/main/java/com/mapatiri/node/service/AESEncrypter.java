package com.mapatiri.node.service;

import com.mapatiri.node.repository.JsonConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Créé à l'aide de l'IA
 */
public class AESEncrypter {

    private final JsonConfigRepository jsonConfigRepository;
    private final Logger logger = LoggerFactory.getLogger(AESEncrypter.class);

    public AESEncrypter(JsonConfigRepository jsonConfigRepository) {
        this.jsonConfigRepository = jsonConfigRepository;
    }

    private SecretKey getSecretKey() {
        String key = jsonConfigRepository.getConfig().getBase64AES();
        byte[] keyBytes = Base64.getDecoder().decode(key);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        try {
            // Génération de l'IV afin d'avoir a chaque chiffrage, un message différent
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Création du cipher qui va permettre de chiffrer le message
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), ivSpec);

            // Chiffrage du contenu
            byte[] cipherText = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(ivSpec.getIV())
                    + "."
                    + Base64.getEncoder().encodeToString(cipherText);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            logger.error(e.getMessage());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public String decrypt(String content) {

        return "";
    }
}
