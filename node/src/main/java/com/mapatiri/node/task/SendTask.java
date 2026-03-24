package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.controller.TaskDispatcher;
import com.mapatiri.node.model.*;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DomainUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SendTask implements ActionTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendTask.class);

    private UserRepo userRepo;
    private TagRepo tagRepo;
    private ConnectionStore connectionStore;
    private String sender;
    private String login;
    private List<String> tags;
    private String content;
    private boolean shouldForward;
    private Set<ClientConnection> followers;
    private ConfigRepository configRepository;
    RegexGrammar regexGrammar = new RegexGrammar();

    public SendTask(String sender, List<String> tags, String content, boolean shouldForwrd,
                    ConnectionStore connectionStore, UserRepo userRepo, TagRepo tagRepo, ConfigRepository configRepository) {

        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        String domain = configRepository.getConfig().getCurrentDomain();
        this.sender = sender + "@" + domain;
        this.login = sender;
        this.tags = tags;
        this.content = content;
        this.shouldForward = shouldForwrd;
        this.connectionStore = connectionStore;
        this.configRepository = configRepository;

        LOGGER.debug("SendTask created: sender={}, tags={}, shouldForward={}",
                sender, tags, shouldForwrd);
    }
    private String senderWithDomain() {
        if (sender.contains("@")) {
            return sender;
        }
        String domain = configRepository.getConfig().getCurrentDomain();
        return sender + "@" + domain;
    }

    @Override
    public void executeTask() {
        //String fullSender = senderWithDomain();
        LOGGER.info("Executing SendTask for sender={}", sender);

        String mssg = regexGrammar.buildCommand("MSG", sender, getContent());
        LOGGER.debug("Built message for sender={}: {}", sender, mssg);

        Set<String> allFollowers = getFollowers(sender);
        LOGGER.info("Found {} total followers for sender={}", allFollowers.size(), sender);

        Set<String> localFollowers = allFollowers.stream()
                .filter(f -> !DomainUtils.isDistantDomain(f, sender))
                .collect(Collectors.toSet());
        List<String> folo = allFollowers.stream()
                .filter(f -> !DomainUtils.isDistantDomain(f, sender))
                .collect(Collectors.toList());
        LOGGER.info("Filtered {} local followers ({} distant) for sender={}",
                localFollowers.size(), allFollowers.size() - localFollowers.size(), sender);

        followers = connectionStore.getConnectionsFromLogin(localFollowers);
        LOGGER.info("Resolved {} active connections for local followers of sender={}",
                followers.size(), sender);
//        if(followers.size() ==1) {
//            LOGGER.info("Envoie a swawa");
//            Optional<ClientConnection> fol = connectionStore.get(folo.getFirst());
//            LOGGER.info(folo.getFirst());
//            if (fol.isPresent()) {
//                var lo = fol.get();
//                lo.send(mssg);
//            }
//        }


        for (ClientConnection followerCon : followers) {
            try {
                LOGGER.debug("Sending message to connection={}", followerCon);
                followerCon.send(mssg);
            } catch (Exception e) {
                LOGGER.error("Failed to send message from sender={} to connection={}",
                        sender, followerCon, e);
            }
        }

        LOGGER.info("SendTask execution finished for sender={}", sender);
    }

    @Override
    public boolean shouldForward() {
        LOGGER.debug("shouldForward() called for sender={}, result={}", sender, shouldForward);
        return shouldForward;
    }

    private Set<String> getFollowers(String sender) {
        LOGGER.debug("Resolving followers for sender={}", sender);

        Set<String> followers = new HashSet<>();
        String localDomain = DomainUtils.extractDomain(sender);
        LOGGER.trace("Extracted localDomain={} from sender={}", localDomain, sender);


        User senderUser = userRepo.getUserFromLogin(login);
        LOGGER.debug("Resolved senderUser={} from login={}", senderUser != null ? senderUser.getLogin() : "null", login);

        if (senderUser != null) {
            LOGGER.debug("senderUser.subscribers={}", senderUser.getSubscribers());
            for (String subscriber : senderUser.getSubscribers()) {
                String fullLogin = subscriber.contains("@")
                        ? subscriber
                        : subscriber + "@" + localDomain;
                followers.add(fullLogin);
                LOGGER.trace("Added subscriber={} (fullLogin={}) for sender={}", subscriber, fullLogin, sender);
            }
        } else {
            LOGGER.warn("Could not resolve senderUser for login={}, no user-based followers will be added", login);
        }

        // Tags — inchangé
        List<Tag> tags = tagRepo.getAllTags().tags();
        LOGGER.debug("Loaded {} tags from repository", tags.size());
        for (var tag : tags) {
            if (this.tags.contains(tag.getTag())) {
                LOGGER.trace("Tag {} matched sender tags {}, adding its {} subscribers",
                        tag.getTag(), this.tags, tag.getSubscribers().size());
                followers.addAll(tag.getSubscribers());
            }
        }

        LOGGER.info("Resolved {} unique followers for sender={}", followers.size(), sender);
        return followers;
    }

    public Set<String> getFollowersFromOtherDomain() {
        LOGGER.debug("Resolving followers from other domains for sender={}", sender);
        Set<String> followers = getFollowers(sender);
        Set<String> followersFromOtherDomain = new HashSet<>();
        for (String follower : followers) {
            if (DomainUtils.isDistantDomain(follower, sender)) {
                followersFromOtherDomain.add(follower);
                LOGGER.trace("Follower={} marked as distant for sender={}", follower, sender);
            }
        }
        LOGGER.info("Found {} followers from distant domains for sender={}",
                followersFromOtherDomain.size(), sender);
        return followersFromOtherDomain;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}
