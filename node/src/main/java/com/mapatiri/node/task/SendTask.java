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


        LOGGER.debug("SendTask created: sender={}, tags={}, shouldForward={}",
                sender, tags, shouldForwrd);
    }

    @Override
    public void executeTask() {
        //String fullSender = senderWithDomain();
        LOGGER.info("Executing SendTask for sender={}", sender);

        String mssg = regexGrammar.buildCommand("MSG", sender, getContent());
        LOGGER.debug("Built message for sender={}: {}", sender, mssg);

        Set<String> localFollowers = getLocalFollowers();
        localFollowers.addAll(getLocalTagsFollowers());
        var follow  = connectionStore.getConnectionsFromLogin(localFollowers);
        LOGGER.info("Resolved {} active connections for local followers of sender={}",
                follow.size(), sender);
        for (ClientConnection followerCon : follow) {
            try {
                LOGGER.debug("Sending message to connection={}", followerCon);
                followerCon.send(mssg);
                LOGGER.debug("FINISH mmsg was sended to connection={}", followerCon);
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

    private Set<String> getLocalFollowers() {
        Set<String> subscribers = userRepo.getUserFromLogin(login).getSubscribers();
        LOGGER.debug("getLocalFollowers Resolving followers for sender={}", sender);
        return subscribers.stream()
                .filter(f -> !DomainUtils.isDistantDomain(f, sender))
                .map(f -> f.contains("@") ? f.substring(0, f.indexOf('@')) : f)
                .collect(Collectors.toSet());
    }
    private Set<String> getLocalTagsFollowers() {
        Set<String> followers = new HashSet<>();
        List<Tag> tags = tagRepo.getAllTags().tags();
        LOGGER.debug("Loaded {} tags from repository", tags.size());
        for (var tag : tags) {
            if (this.tags.contains(tag.getTag())) {
                LOGGER.trace("Tag {} matched sender tags {}, adding its {} subscribers",
                        tag.getTag(), this.tags, tag.getSubscribers().size());
                followers.addAll(tag.getSubscribers().stream()
                        .filter(f -> !DomainUtils.isDistantDomain(f, sender))
                        .map(f -> f.contains("@") ? f.substring(0, f.indexOf('@')) : f)
                        .filter(f -> !f.equals(this.login))
                        .collect(Collectors.toSet()));
            }
        }
        return followers;
    }

    private Set<String> getFollowers(String sender) {
        LOGGER.debug("Resolving followers for sender={}", sender);

        Set<String> followers = new HashSet<>();
        String localDomain = DomainUtils.extractDomain(sender);
        LOGGER.debug("Extracted localDomain={} from sender={}", localDomain, sender);

        List<User> users = userRepo.getAllUsers().users();
        LOGGER.debug("Loaded {} users from repository", users.size());
        for (User user : users) {
            if (userRepo.getUserFromLogin(login).getSubscribers().contains(user.getLogin())) {
                String login = user.getLogin();
//                String fullLogin = login.contains("@")
//                        ? login
//                        : login + "@" + localDomain;
                followers.add(login);
                LOGGER.trace("Added user follower login={} (fullLogin={}) for sender={}",
                        login, login, sender);
            }
        }
//todo a chnager

//        List<Tag> tags = tagRepo.getAllTags().tags();
//        LOGGER.debug("Loaded {} tags from repository", tags.size());
//        for (var tag : tags) {
//            if (this.tags.contains(tag.getTag())) {
//                LOGGER.trace("Tag {} matched sender tags {}, adding its {} subscribers",
//                        tag.getTag(), this.tags, tag.getSubscribers().size());
//                followers.addAll(tag.getSubscribers());
//            }
//        }

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
