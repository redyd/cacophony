package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.service.AESEncrypter;
import com.mapatiri.node.service.SpeakerBridge;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TaskRepeater {
    private RegexGrammar regexGrammar = new RegexGrammar();
    private final SpeakerBridge speakerBridge;
    private final AESEncrypter aesEncrypter;

    @Inject
    public TaskRepeater(SpeakerBridge speakerBridge, AESEncrypter aesEncrypter) {
        this.speakerBridge = speakerBridge;
        this.aesEncrypter = aesEncrypter;
    }

    public void convertAndSend(ActionTask task) throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        String post = "";

        if (task instanceof SendTask sendTask) {
            String id = regexGrammar.generateIdAtDomain(sendTask.getSender());

            String message = regexGrammar.buildCommand("MSG", sendTask.getSender(), sendTask.getContent());
            for (String follower : sendTask.getFollowersFromOtherDomain()) {

                 post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), follower, message);
            }

        } else if (task instanceof SubscribeTask subscribeTask) {
            String message = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
             post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), message);


        }
        String encrypt = aesEncrypter.encrypt(post);
        speakerBridge.forward(encrypt);

    }



}
