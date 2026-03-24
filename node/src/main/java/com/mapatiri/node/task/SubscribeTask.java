package com.mapatiri.node.task;

import com.mapatiri.node.model.Tag;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;


public class SubscribeTask implements ActionTask {

    private UserRepo userRepo;
    private TagRepo tagRepo;
    private String sender;
    private String toSubscribe;
    private boolean shouldForward;
    private final static Logger LOGGER = LoggerFactory.getLogger(SubscribeTask.class);

    public SubscribeTask(UserRepo repo, TagRepo tagRepo, String sender, String toSubscribe, boolean shouldForward) {
        this.userRepo = repo;
        this.tagRepo = tagRepo;
        this.sender = sender;
        this.toSubscribe = toSubscribe;
        this.shouldForward = shouldForward;

        LOGGER.info("SubscribeTask created: sender={}, toSubscribe={}, shouldForward={}",
                sender, toSubscribe, shouldForward);
    }

    @Override
    public void executeTask() {
        LOGGER.info("Executing SubscribeTask: sender={} subscribing to={}", sender, toSubscribe);

        try {
            if (toSubscribe.startsWith("#")) {
                LOGGER.info("Processing tag subscription: sender={} -> tag={}", sender, toSubscribe);

                LOGGER.debug("Adding tag if missing: {}", toSubscribe);
                tagRepo.addTag(new Tag(toSubscribe));

                LOGGER.debug("Adding sender={} as subscriber to tag={}", sender, toSubscribe);
                tagRepo.addSubscriberToTag(toSubscribe, sender);

                LOGGER.debug("Adding tag={} to sender={} user tags", toSubscribe, sender);
                userRepo.addUserTag(sender, toSubscribe);

                LOGGER.info("Successfully subscribed sender={} to tag={}", sender, toSubscribe);
            } else {
                // User subscription
                LOGGER.info("Processing user subscription: sender={} -> user={}", sender, toSubscribe);

                LOGGER.debug("Adding sender={} as subscriber to user={}", sender, toSubscribe);
                userRepo.addSubscriberToUser(sender, toSubscribe);

                LOGGER.info("Successfully subscribed sender={} to user={}", sender, toSubscribe);
            }
        } catch (IOException e) {
            LOGGER.error( "Failed to process subscription: sender={} to={}",
                    new Object[]{sender, toSubscribe}, e);
        }

        LOGGER.info("SubscribeTask execution finished for sender={}", sender);
    }

    @Override
    public boolean shouldForward() {
        LOGGER.info("shouldForward() called for sender={}, result={}", sender, shouldForward);
        return shouldForward;
    }

    public String getSender() {
        return sender;
    }

    public String getToSubscribe() {
        return toSubscribe;
    }
}
