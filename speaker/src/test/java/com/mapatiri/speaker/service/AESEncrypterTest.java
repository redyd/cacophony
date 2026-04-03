package com.mapatiri.speaker.service;

import com.google.gson.Gson;
import com.mapatiri.speaker.model.SpeakerConfig;
import com.mapatiri.speaker.repository.SpeakerConfigRepository;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AESEncrypterTest {

    @Test
    void encryptThenDecryptReturnsOriginalMessage() {
        SpeakerConfigRepository repository = mockRepositoryWithConfig();
        AESEncrypter aesEncrypter = new AESEncrypter(repository);

        String plainText = "POST 1@source.test alice1@source.test bob22@dest.test hello\r\n";
        String encrypted = aesEncrypter.encrypt("source.test", plainText);
        String decrypted = aesEncrypter.decrypt("source.test", encrypted);

        assertNotEquals("", encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptThrowsWhenDomainIsUnknown() {
        SpeakerConfigRepository repository = mockRepositoryWithConfig();
        AESEncrypter aesEncrypter = new AESEncrypter(repository);

        assertThrows(IllegalArgumentException.class, () -> aesEncrypter.encrypt("unknown.test", "hello"));
    }

    @Test
    void decryptReturnsEmptyStringWhenCiphertextIsInvalid() {
        SpeakerConfigRepository repository = mockRepositoryWithConfig();
        AESEncrypter aesEncrypter = new AESEncrypter(repository);

        String invalidPayload = Base64.getEncoder().encodeToString(new byte[16])
                + ":"
                + Base64.getEncoder().encodeToString("bad-cipher".getBytes(StandardCharsets.UTF_8));

        assertEquals("", aesEncrypter.decrypt("source.test", invalidPayload));
    }

    private SpeakerConfigRepository mockRepositoryWithConfig() {
        String json = """
                {
                  "multicastAddress": "224.224.200.200",
                  "multicastPort": 11003,
                  "networkInterface": "eth0",
                  "configuredDomains": [
                    {
                      "domain": "source.test",
                      "base64AES": "1f2682Wgj/OGwmbUCBAH8fuCeKsBYz8E4o+HM4+50is="
                    },
                    {
                      "domain": "dest.test",
                      "base64AES": "x0o9NMfpjiRRrSWzwka+g8vc1zO1BJXcdo3jHCZD+6Y="
                    }
                  ]
                }
                """;
        SpeakerConfig config = new Gson().fromJson(json, SpeakerConfig.class);

        SpeakerConfigRepository repository = mock(SpeakerConfigRepository.class);
        when(repository.getConfig()).thenReturn(config);
        return repository;
    }
}
