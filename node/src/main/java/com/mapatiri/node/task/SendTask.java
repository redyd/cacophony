package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.model.*;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.FollowerUtils;
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
        this.sender = sender;
        this.login = DomainUtils.getLogin(sender);
        this.tags = tags;
        this.content = content;
        this.shouldForward = shouldForwrd;
        this.connectionStore = connectionStore;


        LOGGER.debug("SendTask created: sender={}, tags={}, shouldForward={}",
                sender, tags, shouldForwrd);
    }

    @Override
    public void executeTask() {
        LOGGER.info("Executing SendTask for sender={}", sender);

        String mssg = regexGrammar.buildCommand("MSG", sender, getContent());
        LOGGER.debug("Built message for sender={}: {}", sender, mssg);

        Set<String> localFollowers = new HashSet<>(getLocalFollowers());
        localFollowers.addAll(getLocalTagsFollowers());

        var connectionsFromLogin = connectionStore.getConnectionsFromLogin(localFollowers);
        LOGGER.info("Resolved {} active connections for local followers of sender={}",
                connectionsFromLogin.size(), sender);
        for (ClientConnection followerCon : connectionsFromLogin) {
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

    private Set<String> getLocalFollowers() {
        User user = userRepo.getUserFromLogin(login);
        if(user == null) {
            LOGGER.error("User not found for login={}", login);
            return Set.of();
        }

        return FollowerUtils.filterSubscribers(
                user.getSubscribers(), sender, login, false);
    }

    private Set<String> getLocalTagsFollowers() {
        LOGGER.debug("getLocalTagsFollowers: sender={}", sender);
        return FollowerUtils.filterTagSubscribers(
                tagRepo.getAllTags().tags(), this.tags, sender, login, false);
    }

    private Set<String> getDistantFollowers() {
        User user = userRepo.getUserFromLogin(login);
        if(user == null) {
            LOGGER.error("User not found for login={}", login);
            return Set.of();
        }

        return FollowerUtils.filterSubscribers(
                user.getSubscribers(), sender, login, true);
    }

    private Set<String> getDistantTagsFollowers() {
        LOGGER.debug("getDistantTagsFollowers: sender={}", sender);
        return FollowerUtils.filterTagSubscribers(
                tagRepo.getAllTags().tags(), this.tags, sender, login, true);
    }

    public Set<String> getFollowersFromOtherDomain() {
        LOGGER.debug("Resolving followers from other domains for sender={}", sender);
        Set<String> followers = new HashSet<>(getDistantFollowers());
        followers.addAll(getDistantTagsFollowers());
        return followers;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}
