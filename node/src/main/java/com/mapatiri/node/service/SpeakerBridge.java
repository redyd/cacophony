package com.mapatiri.node.service;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.repository.JsonConfigRepository;

import java.net.SocketAddress;
import java.util.logging.Handler;

public class SpeakerBridge {

    private final AESEncrypter aes;
    private final RegexGrammar regexGrammar;

    public SpeakerBridge(AESEncrypter aes, RegexGrammar regexGrammar) {
        this.aes = aes;
        this.regexGrammar = regexGrammar;
    }

    public void forward(String message) {

    }

    public void startListening(SocketAddress socketAddress) {

    }
}
