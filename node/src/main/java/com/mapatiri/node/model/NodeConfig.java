package com.mapatiri.node.model;

import java.nio.file.Path;

public class NodeConfig {

    public static final Path KEYSTORE_PATH = Path.of("node", "src", "main", "resources", "data", "keystore.p12");

    //le nom du domaine du node
    private String currentDomain;
    private int saltSizeInBytes;
    private String multicastAddress;
    private int multicastPort;
    private int unicastPort;
    private int relayPort;
    private String networkInterface;
    private String base64AES;
    private boolean tls;
    private String keystorePassword;

    public NodeConfig() {
    }

    public String getCurrentDomain() {
        return currentDomain;
    }

    public int getSaltSizeInBytes() {
        return saltSizeInBytes;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public int getUnicastPort() {
        return unicastPort;
    }

    public int getRelayPort() {
        return relayPort;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public String getBase64AES() {
        return base64AES;
    }

    public boolean isTls() {
        return tls;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }
}