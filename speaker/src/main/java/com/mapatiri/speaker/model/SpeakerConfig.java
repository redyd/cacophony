package com.mapatiri.speaker.model;

import java.util.List;

public class SpeakerConfig {

    private String multicastAddress;
    private int multicastPort;
    private String networkInterface;
    private List<ConfiguredDomain>  configuredDomains;

    public SpeakerConfig() {

    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public List<ConfiguredDomain> getConfiguredDomains() {
        return configuredDomains;
    }

}
