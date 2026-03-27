package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.service.SpeakerBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class TaskRepeater {
    private RegexGrammar regexGrammar = new RegexGrammar();
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepeater.class);
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

            Set<String> distantFollowers = sendTask.getFollowersFromOtherDomain();
            LOGGER.info("Forwarding message from {} to {} distant followers", sendTask.getSender(), distantFollowers.size());
            for (String follower : distantFollowers) {
                post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), follower, message);
                LOGGER.debug("Forwarding POST to distant follower={}", follower);
                speakerBridge.forward(post);
            }

            Set<String> distantTags = sendTask.getDistantTags();
            LOGGER.info("Forwarding message from {} to {} distant tags", sendTask.getSender(), distantTags.size());
            for (String distantTag : distantTags) {
                post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), distantTag, message);
                LOGGER.debug("Forwarding POST to distant tag={}", distantTag);
                speakerBridge.forward(post);
            }

        } else if (task instanceof SubscribeTask subscribeTask) {
            LOGGER.info("Forwarding SUBSCRIBE from {} to {}", subscribeTask.getSender(), subscribeTask.getToSubscribe());
            String message = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
            post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), message);
            LOGGER.debug("Forwarding POST for subscribe: {}", post);
            speakerBridge.forward(post);

        } else {
            LOGGER.warn("Unknown task type received: {}", task.getClass().getSimpleName());
        }
    }



}
