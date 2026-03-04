package com.mapatiri.node.repository;

import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.model.NodeConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JsonConfigRepositoryTest {

    @Test
    void testLoadConfig() {
        var repo = new JsonConfigRepository();
        try {
            repo.loadConfig(Path.of("src", "main", "resources", "node-config.json"));
            var conf = repo.getConfig();

            assertNotNull(conf);
            assertNotNull(conf.getCurrentDomain());
            assertNotEquals(0, conf.getSaltSizeInBytes());
            assertNotNull(conf.getMulticastAddress());
            assertNotEquals(0, conf.getMulticastPort());
            assertNotEquals(0, conf.getUnicastPort());
            assertNotEquals(0, conf.getRelayPort());
            assertNotNull(conf.getNetworkInterface());
            assertNotNull(conf.getBase64AES());
            assertNotNull(conf.getKeystorePassword());
        } catch (NodeFileException e) {
            fail();
        }
    }
}
