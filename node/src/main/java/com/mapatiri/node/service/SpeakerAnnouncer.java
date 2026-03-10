package com.mapatiri.node.service;

import com.mapatiri.node.repository.JsonConfigRepository;

import java.net.MulticastSocket;

public class SpeakerAnnouncer implements Clockable {

    private final JsonConfigRepository jsonConfigRepository;
    private final SpeakerBridge speakerBridge;
    private final MulticastSocket multicastSocket;

    public SpeakerAnnouncer(JsonConfigRepository jsonConfigRepository, SpeakerBridge speakerBridge, MulticastSocket multicastSocket) {
        this.jsonConfigRepository = jsonConfigRepository;
        this.speakerBridge = speakerBridge;
        this.multicastSocket = multicastSocket;
    }

    @Override
    public void onClockSignal() {

    }

    public void waitForResponse() {

    }
}
