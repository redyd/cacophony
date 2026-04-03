package com.mapatiri.speaker.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfiguredDomainTest {

    @Test
    void gettersReturnValuesFromJson() {
        String json = """
                {
                  "domain": "node1.swilabus.xyz",
                  "base64AES": "abc123=="
                }
                """;

        ConfiguredDomain domain = new Gson().fromJson(json, ConfiguredDomain.class);

        assertEquals("node1.swilabus.xyz", domain.getDomain());
        assertEquals("abc123==", domain.getBase64AES());
    }
}
