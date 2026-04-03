package com.mapatiri.speaker.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpeakerConfigTest {

    @Test
    void gettersReturnValuesFromJson() {
        String json = """
                {
                  "multicastAddress": "224.224.200.200",
                  "multicastPort": 11003,
                  "networkInterface": "eth0",
                  "configuredDomains": [
                    {
                      "domain": "node1.swilabus.xyz",
                      "base64AES": "abc123=="
                    }
                  ]
                }
                """;

        SpeakerConfig config = new Gson().fromJson(json, SpeakerConfig.class);

        assertEquals("224.224.200.200", config.getMulticastAddress());
        assertEquals(11003, config.getMulticastPort());
        assertEquals("eth0", config.getNetworkInterface());
        assertNotNull(config.getConfiguredDomains());
        assertEquals(1, config.getConfiguredDomains().size());
        assertEquals("node1.swilabus.xyz", config.getConfiguredDomains().getFirst().getDomain());
    }
}
