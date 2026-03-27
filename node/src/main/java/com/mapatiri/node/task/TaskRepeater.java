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


        if (task instanceof SendTask sendTask) {
            String id = regexGrammar.generateIdAtDomain(sendTask.getSender());

            String mssg = regexGrammar.buildCommand("MSG", sendTask.getSender(), sendTask.getContent());
            String aesMssg = mssg;
            for (String follower : sendTask.getFollowersFromOtherDomain()) {

                String post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), follower, aesMssg);
                speakerBridge.forward(post);
            }

        } else if (task instanceof SubscribeTask subscribeTask) {
            String mssg = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String aesMssg = mssg;
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
            String post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), aesMssg);

            String encrypt = aesEncrypter.encrypt(post);
            speakerBridge.forward(encrypt);
        }

    }
    //todo a mettre dans le la grammaire


}
