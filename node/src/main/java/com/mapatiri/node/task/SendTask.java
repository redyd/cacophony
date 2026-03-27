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
import java.util.stream.Collectors;

public class SendTask implements ActionTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendTask.class);

    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final ConnectionStore connectionStore;
    private final String sender;
    private final String login;
    private final List<String> tags;
    private final String content;
    private final boolean shouldForward;

    public SendTask(String sender, List<String> tags, String content, boolean shouldForward, ConnectionStore connectionStore, UserRepo userRepo, TagRepo tagRepo, ConfigRepository configRepository) {
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        String domain = configRepository.getConfig().getCurrentDomain();
        this.sender = sender + "@" + domain;
        this.login = sender;
        this.tags = tags;
        this.content = content;
        this.shouldForward = shouldForward;
        this.connectionStore = connectionStore;
    }

    @Override
    public void executeTask() {
        RegexGrammar regexGrammar = new RegexGrammar();
        String mssg = regexGrammar.buildCommand("MSG", sender, getContent());

        Set<String> localFollowers = getLocalFollowers();
        localFollowers.addAll(getLocalTagsFollowers());
        var activeUsers = connectionStore.getConnectionsFromLogin(localFollowers);

        for (ClientConnection currentUser : activeUsers) {
            try {
                currentUser.send(mssg);
            } catch (Exception e) {
                LOGGER.error("Message from '{}' could not be heard by '{}'", sender, currentUser.getUser().getLogin(), e);
            }
        }

        LOGGER.info("A message has been sent to {} connected users", activeUsers.size());
    }

    @Override
    public boolean shouldForward() {
        return shouldForward;
    }

    private Set<String> getLocalFollowers() {
        return FollowerUtils.filterSubscribers(userRepo.getUserFromLogin(login).getSubscribers(), sender, login, false);
    }

    /**
     * la fct retourne les abbonés au tag utilisé dans le mssg, et il faut que le user soit abboné au tag pour qu'il soit considéré
     * @return
     */
    private Set<String> getLocalTagsFollowers() {
        LOGGER.debug("getLocalTagsFollowers: sender={}", sender);

        Set<String> userSubscribedTags = userRepo.getUserFromLogin(login).getUserTags()
                .stream()
                .map(FollowerUtils::stripDomain)
                .collect(Collectors.toSet());

        List<String> allowedTags = this.tags.stream()
                .filter(userSubscribedTags::contains)
                .collect(Collectors.toList());

        LOGGER.debug("getLocalTagsFollowers: used={}, allowed={}", this.tags, allowedTags);

        Set<String> result = FollowerUtils.filterTagSubscribers(
                tagRepo.getAllTags().tags(), allowedTags, sender, login, false);

        LOGGER.debug("getLocalTagsFollowers: resolved subscribers={}", result);
        return result;
    }

    private Set<String> getDistantFollowers() {
        return FollowerUtils.filterSubscribers(userRepo.getUserFromLogin(login).getSubscribers(), sender, login, true);
    }

    /**
     * ça retourne les followers externe des tags local qui sont utilisé dans le message
     * @return
     */
    private Set<String> getDistantTagsFollowers() {
        return FollowerUtils.filterTagSubscribers(tagRepo.getAllTags().tags(), this.tags, sender, login, true);
    }

    public Set<String> getFollowersFromOtherDomain() {
        Set<String> followers = getDistantFollowers();
        followers.addAll(getDistantTagsFollowers());
        return followers;
    }

    /**
     * Retourne les tags utilisé dans le message mais qui provienent d'un autre domaine
     * @return
     */
    public Set<String> getDistantTags() {
        Set<String> distantTags = new HashSet<>();
        Set<String> userTags = userRepo.getUserFromLogin(login).getUserTags();
        String localDomain = sender.substring(sender.indexOf('@') + 1);
        LOGGER.debug("getDistantTags: checking {} user tags for sender={}", userTags.size(), sender);

        for (String tag : userTags) {
            String tagWithoutDomain = FollowerUtils.stripDomain(tag);
            String tagDomain = tag.contains("@") ? tag.substring(tag.indexOf('@') + 1) : localDomain;

            if (tags.contains(tagWithoutDomain) && !tagDomain.equals(localDomain)) {
                LOGGER.debug("Distant tag matched: {} (stripped={})", tag, tagWithoutDomain);
                distantTags.add(tag);
            }
        }

        LOGGER.info("getDistantTags: found {} distant tags for sender={}", distantTags.size(), sender);
        return distantTags;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}