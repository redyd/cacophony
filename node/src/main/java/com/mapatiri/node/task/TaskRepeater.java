package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.service.SpeakerBridge;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TaskRepeater {
    private RegexGrammar regexGrammar = new RegexGrammar();
    private final SpeakerBridge speakerBridge;

    @Inject
    public TaskRepeater(SpeakerBridge speakerBridge) {
        this.speakerBridge = speakerBridge;
    }

    public void convertAndSend(ActionTask task) throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        String post = "";

        if (task instanceof SendTask sendTask) {
            String id = regexGrammar.generateIdAtDomain(sendTask.getSender());

            String message = regexGrammar.buildCommand("MSG", sendTask.getSender(), sendTask.getContent());
            for (String follower : sendTask.getFollowersFromOtherDomain()) {

                 post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), follower, message);
                speakerBridge.forward(post);
            }
            //envoyé le post au déstinataire -> le tag distant comme ça on peut savoir les subscribers du tag grace au repo du domaine de ce tag
            for(String distantTag : sendTask.getDistantTags()) {
                post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), distantTag, message);
                speakerBridge.forward(post);
            }

        } else if (task instanceof SubscribeTask subscribeTask) {
            String message = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
             post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), message);
            speakerBridge.forward(post);


        }
//        String encrypt = aesEncrypter.encrypt(post);


    }



}
