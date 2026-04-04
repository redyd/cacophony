package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.Msg;
import com.mapatiri.grammar.parse.Post;
import com.mapatiri.grammar.parse.Subscribe;
import com.mapatiri.node.service.SpeakerBridge;
import com.mapatiri.node.service.SpeakerBridgeService;
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
    private final SpeakerBridgeService speakerBridge;

    @Inject
    public TaskRepeater(SpeakerBridgeService speakerBridge) {
        this.speakerBridge = speakerBridge;
        this.regexGrammar = new RegexGrammar();
    }

    public void convertAndSend(ActionTask task) {
        String post;

        if (task instanceof SendTask sendTask) {
            String id = regexGrammar.generateIdAtDomain(sendTask.getSender());
            String message = regexGrammar.buildContent(new Msg(sendTask.getSender(), sendTask.getContent()));

            Set<String> distantFollowers = sendTask.getFollowersFromOtherDomain();
            LOGGER.info("Forwarding a message from {} to {} distant followers", sendTask.getSender(), distantFollowers.size());
            for (String follower : distantFollowers) {
                post = regexGrammar.build(new Post(id, sendTask.getSender(), follower, message));
                speakerBridge.forward(post);
            }

            Set<String> distantTags = sendTask.getDistantTags();
            LOGGER.info("Forwarding a message from {} to {} distant tags", sendTask.getSender(), distantTags.size());
            for (String distantTag : distantTags) {
                post = regexGrammar.build(new Post(id, sendTask.getSender(), distantTag, message));
                speakerBridge.forward(post);
            }

        } else if (task instanceof SubscribeTask subscribeTask) {
            LOGGER.info("Forwarding a subscribe command from {} to {}", subscribeTask.getSender(), subscribeTask.getToSubscribe());
            String message = regexGrammar.buildContent(new Subscribe(subscribeTask.getToSubscribe()));
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
            post = regexGrammar.build(new Post(id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), message));
            speakerBridge.forward(post);
        } else {
            LOGGER.warn("Unknown task type received: {}", task.getClass().getSimpleName());
        }
    }

}
