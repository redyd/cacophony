package com.mapatiri.speaker.service;

import com.mapatiri.grammar.RegexGrammar;
import org.checkerframework.checker.regex.qual.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SpeakerServer {
    private static final Logger logger = LoggerFactory.getLogger(SpeakerServer.class);

    private final RegexGrammar regexGrammar = new RegexGrammar();

    public SpeakerServer() {

    }

    public void startConnectionFor(InetAddress nodeIp, String domain, int port) {
        try(Socket socket = new Socket(domain, port)) {
            logger.info("Connected to " + socket.getInetAddress().getHostName());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleNode(InetAddress nodeIp, String domain, int port) {

    }
}
