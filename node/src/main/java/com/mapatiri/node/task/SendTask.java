package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.Msg;
import com.mapatiri.grammar.parse.Subscribe;
import com.mapatiri.node.model.*;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tâche d'envoi de message.
 *
 * @author Riham
 */
public class SendTask implements ActionTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendTask.class);

    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final ConnectionStore connectionStore;
    private final IdentityUtils identityUtils;
    private final String sender;
    private final String login;
    private final List<String> tags;
    private final String content;
    private final boolean shouldForward;

    public SendTask(
            String sender,
            List<String> tags,
            String content,
            boolean shouldForward,
            ConnectionStore connectionStore,
            UserRepo userRepo,
            TagRepo tagRepo,
            ConfigRepository configRepository,
            IdentityUtils identityUtils) {
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        String domain = configRepository.getConfig().getCurrentDomain();
        if (sender.contains("@")) {
            this.sender = sender;
            this.login = sender.substring(0, sender.indexOf('@'));
        } else {
            this.sender = sender + "@" + domain;
            this.login = sender;
        }
        this.tags = tags;
        this.content = content;
        this.shouldForward = shouldForward;
        this.connectionStore = connectionStore;
        this.identityUtils = identityUtils;
    }

    @Override
    public void executeTask() {
        RegexGrammar regexGrammar = new RegexGrammar();
        String mssg = regexGrammar.build(new Msg( sender, getContent()));

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
        User user = userRepo.getUserFromLogin(login);
        if (user == null) {
            return userRepo.getLocalSubscribersOf(sender);
        }
        return identityUtils.filterSubscribers(userRepo.getUserFromLogin(login).getSubscribers(), sender, login, false);
    }

    /**
     * la fct retourne les abonnés au tag utilisé dans le mssg, et il faut que le user soit abonné au tag pour qu'il soit considéré
     */
    private Set<String> getLocalTagsFollowers() {
        Set<String> userSubscribedTags = getSenderSubscribedTags();

        List<String> allowedTags = this.tags.stream()
                .filter(userSubscribedTags::contains)
                .collect(Collectors.toList());

        return identityUtils.filterTagSubscribers(
                tagRepo.getAllTags().tags(), allowedTags, sender, login, false);
    }

    private Set<String> getDistantFollowers() {
        return identityUtils.filterSubscribers(userRepo.getUserFromLogin(login).getSubscribers(), sender, login, true);
    }

    /**
     * ça retourne les followers externe des tags local qui sont utilisé dans le message
     */
    private Set<String> getDistantTagsFollowers() {
        Set<String> senderSubscribedTags = getSenderSubscribedTags();

        List<String> allowedTags = this.tags.stream()
                .filter(senderSubscribedTags::contains)
                .collect(Collectors.toList());

        return identityUtils.filterTagSubscribers(
                tagRepo.getAllTags().tags(), allowedTags, sender, login, true);
    }

    public Set<String> getFollowersFromOtherDomain() {
        Set<String> followers = getDistantFollowers();
        followers.addAll(getDistantTagsFollowers());
        return followers;
    }

    /**
     * Retourne les tags utilisé dans le message mais qui provienent d'un autre domaine
     */
    public Set<String> getDistantTags() {
        String localDomain = sender.substring(sender.indexOf('@') + 1);
        Set<String> senderSubscribedTags = getSenderSubscribedTags();

        return tagRepo.getAllTags().tags().stream()
                .filter(tag -> tags.contains(tag.getTag()))
                .filter(tag -> senderSubscribedTags.contains(tag.getTag()))
                .filter(tag -> !tag.getDomain().equals(localDomain))
                .map(tag -> tag.getTag() + "@" + tag.getDomain())
                .collect(Collectors.toSet());
    }
    private Set<String> getSenderSubscribedTags() {
        return tagRepo.getAllTags().tags().stream()
                .filter(tag -> tag.getSubscribers().contains(sender))
                .map(Tag::getTag)
                .collect(Collectors.toSet());
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}