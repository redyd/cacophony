package com.mapatiri.node.task;

import com.mapatiri.node.model.Tag;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.FollowerUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;

/**
 * LOGGER écrit par l'ia
 */
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
                subscribeToTag();
            } else {
                subscribeToUser();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to process subscription: sender={} to={}", sender, toSubscribe, e);
        }
        LOGGER.info("SubscribeTask execution finished for sender={}", sender);
    }

    private void subscribeToTag() throws IOException {
        LOGGER.info("Processing tag subscription: sender={} -> tag={}", sender, toSubscribe);

        String tagName = FollowerUtils.stripDomain(toSubscribe);
        Tag tag = new Tag(tagName);

        if (!tagRepo.tagExist(tag)) {
            tagRepo.addTag(tag);
        }

        LOGGER.debug("Adding sender={} as subscriber to tag={}", sender, tagName);
        tagRepo.addSubscriberToTag(tagName, sender);

        String senderLogin = FollowerUtils.stripDomain(sender);
        LOGGER.debug("Adding tag={} to sender={} user tags", toSubscribe, senderLogin);
        userRepo.addUserTag(senderLogin, toSubscribe);

        LOGGER.info("Successfully subscribed sender={} to tag={}", sender, toSubscribe);
    }

    private void subscribeToUser() throws IOException {
        LOGGER.info("Processing user subscription: sender={} -> user={}", sender, toSubscribe);

        String senderLogin = FollowerUtils.stripDomain(sender);
        LOGGER.debug("Adding sender={} as subscriber to user={}", sender, toSubscribe);
        userRepo.addSubscriberToUser(senderLogin, toSubscribe);

        LOGGER.info("Successfully subscribed sender={} to user={}", sender, toSubscribe);
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
