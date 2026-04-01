package com.mapatiri.node.task;

import com.mapatiri.node.model.Tag;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.IdentityUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Tâche d'abonnement : se charge d'abonner un utilisateur à une personne ou un tag
 *
 * @author Riham
 */
public class SubscribeTask implements ActionTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(SubscribeTask.class);

    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final IdentityUtils identityUtils;

    private final String sender;
    private final String toSubscribe;
    private final boolean shouldForward;

    public SubscribeTask(UserRepo repo, TagRepo tagRepo, IdentityUtils identityUtils, String sender, String toSubscribe, boolean shouldForward ) {
        this.userRepo = repo;
        this.tagRepo = tagRepo;
        this.sender = sender;
        this.toSubscribe = toSubscribe;
        this.shouldForward = shouldForward;
        this.identityUtils = identityUtils;

    }

    @Override
    public void executeTask() {
        try {
            if (toSubscribe.startsWith("#")) {
                subscribeToTag();
            } else {
                subscribeToUser();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to process subscription for {}", sender, e);
        }
        LOGGER.info("Subscription done for {}", sender);
    }

    private void subscribeToTag() throws IOException {
        String tagName = identityUtils.stripDomain(toSubscribe);
        String domain = identityUtils.extractDomain(toSubscribe);
        Tag tag = new Tag(tagName,  domain);

        if (!tagRepo.tagExist(tag)) {
            tagRepo.addTag(tag);
        }

        tagRepo.addSubscriberToTag(tagName, sender);

//        String senderLogin = identityUtils.stripDomain(sender);
//        userRepo.addUserTag(senderLogin, toSubscribe);
    }

    private void subscribeToUser() throws IOException {
        String toSubscribeLogin = identityUtils.stripDomain(toSubscribe);
        userRepo.addSubscriberToUser(toSubscribeLogin,sender );
    }


    @Override
    public boolean shouldForward() {
        return shouldForward;
    }

    public String getSender() {
        return sender;
    }

    public String getToSubscribe() {
        return toSubscribe;
    }
}
