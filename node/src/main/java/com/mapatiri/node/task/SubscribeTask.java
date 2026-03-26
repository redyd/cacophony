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
            var senderWithoutDomain = "";
            if (toSubscribe.startsWith("#")) {
                LOGGER.info("Processing tag subscription: sender={} -> tag={}", sender, toSubscribe);

                LOGGER.debug("Adding tag if missing: {}", toSubscribe);
                //quand on veut ajouter un tag dans le tag repo on doit enlever son domaine et seulement mentionner le nom
                String tagWithoutDomainString = toSubscribe.contains("@") ? toSubscribe.substring(0, toSubscribe.indexOf('@')) : toSubscribe;
                Tag tagWithoutDomain = new Tag(tagWithoutDomainString);
                LOGGER.debug("TAG WITHOUT A DOMAIN" + tagWithoutDomainString);
                if (!tagRepo.tagExist(tagWithoutDomain)) {
                    tagRepo.addTag(tagWithoutDomain);
                }
                LOGGER.debug("Adding sender={} as subscriber to tag={}", sender, tagWithoutDomainString);
                tagRepo.addSubscriberToTag(tagWithoutDomainString, sender);

                //quand dans le user repo on doit preciser quelle tag l'utilisateur suit on doit preciser le domaine
                LOGGER.debug("Adding tag={} to sender={} user tags", toSubscribe, sender);
                senderWithoutDomain = sender.substring(0, sender.indexOf('@'));
                userRepo.addUserTag(senderWithoutDomain, toSubscribe);

                LOGGER.info("Successfully subscribed sender={} to tag={}", sender, toSubscribe);
            } else {
                LOGGER.info("Processing user subscription: sender={} -> user={}", sender, toSubscribe);

                LOGGER.debug("Adding sender={} as subscriber to user={}", sender, toSubscribe);
                userRepo.addSubscriberToUser(senderWithoutDomain, toSubscribe);

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
