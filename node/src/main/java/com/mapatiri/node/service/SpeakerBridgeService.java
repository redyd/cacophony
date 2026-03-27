package com.mapatiri.node.service;

import java.net.Socket;

public interface SpeakerBridgeService {
    void forward(String message);
    void startListening(Socket socket);
}
