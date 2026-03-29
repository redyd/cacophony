package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.service.SpeakerBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Convertit une {@link ActionTask} en message POST et le propage dans le {@link SpeakerBridge}.
 *
 * @author Riham
 */
public class TaskRepeater {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepeater.class);

    private final RegexGrammar regexGrammar;
    private final SpeakerBridge speakerBridge;

    @Inject
    public TaskRepeater(SpeakerBridge speakerBridge) {
        this.speakerBridge = speakerBridge;
        this.regexGrammar = new RegexGrammar();
    }

    public void convertAndSend(ActionTask task) {
        String post;

        if (task instanceof SendTask sendTask) {
            String id = regexGrammar.generateIdAtDomain(sendTask.getSender());
            String message = regexGrammar.buildCommand("MSG", sendTask.getSender(), sendTask.getContent());

            Set<String> distantFollowers = sendTask.getFollowersFromOtherDomain();
            LOGGER.info("Forwarding a message from {} to {} distant followers", sendTask.getSender(), distantFollowers.size());
            for (String follower : distantFollowers) {
                post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), follower, message);
                speakerBridge.forward(post);
            }

            Set<String> distantTags = sendTask.getDistantTags();
            LOGGER.info("Forwarding a message from {} to {} distant tags", sendTask.getSender(), distantTags.size());
            for (String distantTag : distantTags) {
                post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), distantTag, message);
                speakerBridge.forward(post);
            }

        } else if (task instanceof SubscribeTask subscribeTask) {
            LOGGER.info("Forwarding a subscribe command from {} to {}", subscribeTask.getSender(), subscribeTask.getToSubscribe());
            String message = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
            post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), message);
            speakerBridge.forward(post);
        } else {
            LOGGER.warn("Unknown task type received: {}", task.getClass().getSimpleName());
        }
    }

}
