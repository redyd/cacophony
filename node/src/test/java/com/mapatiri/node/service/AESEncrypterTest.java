package com.mapatiri.node.service;

import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.JsonConfigRepository;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AESEncrypterTest {

    @Test
    void should_return_another_message_than_original_after_encrypt() throws Exception {
        JsonConfigRepository repo = mock(JsonConfigRepository.class);

        var config = mock(NodeConfig.class);

        byte[] key = new byte[16];
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("CLE AES : " + base64Key);

        when(repo.getConfig()).thenReturn(config);
        when((String) config.getClass().getMethod("getBase64AES").invoke(config)).thenReturn(base64Key);

        AESEncrypter aes = new AESEncrypter(repo);

        String message = "hello world";
        System.out.println("MESSAGE DE BASE : " + message);

        String encrypted = aes.encrypt(message);
        System.out.println("MESSAGE CRYPTE : " + encrypted);

        assertNotEquals(message, encrypted);
        assertNotNull(encrypted);
        assertNotEquals("", encrypted);
    }

    @Test
    void should_return_same_message_after_encrypt_and_decrypt() throws Exception {

        JsonConfigRepository repo = mock(JsonConfigRepository.class);

        var config = mock(NodeConfig.class);

        byte[] key = new byte[16];
        String base64Key = Base64.getEncoder().encodeToString(key);

        when(repo.getConfig()).thenReturn(config);
        when((String) config.getClass().getMethod("getBase64AES").invoke(config)).thenReturn(base64Key);

        AESEncrypter aes = new AESEncrypter(repo);

        String message = "hello world";
        System.out.println("message de base: " + message);

        String encrypted = aes.encrypt(message);
        System.out.println("message crypte: " + encrypted);

        String decrypted = aes.decrypt(encrypted);
        System.out.println("message decrypte: " + decrypted);

        assertEquals(message, decrypted);
        assertNotNull(encrypted);
        assertNotEquals("",  encrypted);
    }
}