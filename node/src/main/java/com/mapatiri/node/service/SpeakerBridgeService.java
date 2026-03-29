package com.mapatiri.node.service;

import java.net.Socket;

/**
 * Fait le pont entre le speaker et le node : écoute et envoi des messages.
 *
 * @author Mattéo
 */
public interface SpeakerBridgeService {
    void forward(String message);
    void startListening(Socket socket);
}
