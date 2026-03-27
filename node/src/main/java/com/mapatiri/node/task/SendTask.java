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

import java.util.*;

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
        LOGGER.info("Executing SendTask for sender={}", sender);

        String mssg = regexGrammar.buildCommand("MSG", sender, getContent());
        LOGGER.debug("Built message for sender={}: {}", sender, mssg);

        Set<String> localFollowers = getLocalFollowers();
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
        LOGGER.debug("getLocalFollowers: sender={}", sender);
        return FollowerUtils.filterSubscribers(
                userRepo.getUserFromLogin(login).getSubscribers(), sender, login, false);
    }

    private Set<String> getLocalTagsFollowers() {
        LOGGER.debug("getLocalTagsFollowers: sender={}", sender);
        return FollowerUtils.filterTagSubscribers(
                tagRepo.getAllTags().tags(), this.tags, sender, login, false);
    }

    private Set<String> getDistantFollowers() {
        LOGGER.debug("getDistantFollowers: sender={}", sender);
        return FollowerUtils.filterSubscribers(
                userRepo.getUserFromLogin(login).getSubscribers(), sender, login, true);
    }

    /**
     * ça retourne les followers externe des tags local qui sont utilisé dans le message
     * @return
     */
    private Set<String> getDistantTagsFollowers() {
        LOGGER.debug("getDistantTagsFollowers: sender={}", sender);
        return FollowerUtils.filterTagSubscribers(
                tagRepo.getAllTags().tags(), this.tags, sender, login, true);
    }

    public Set<String> getFollowersFromOtherDomain() {
        LOGGER.debug("Resolving followers from other domains for sender={}", sender);
        Set<String> followers = getDistantFollowers();
        followers.addAll(getDistantTagsFollowers());
        return followers;
    }

    /**
     * Retourne les tags utilisé dans le message mais qui provienent d'un autre domaine
     * @return
     */
    public Set<String> getDistantTags(){
        Set<String> distantTags = new HashSet<>();
        Set<String> userTags = userRepo.getUserFromLogin(login).getUserTags();
        for (String tag : userTags) {
            String tagWithoutDomain = FollowerUtils.stripDomain(tag);
            if(tags.contains(tagWithoutDomain)){
                distantTags.add(tag);
            }
        }
        return distantTags;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}