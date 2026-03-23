package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.model.*;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import util.DomainUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SendTask implements ActionTask {

    private UserRepo userRepo;
    private TagRepo tagRepo;
    private ConnectionStore connectionStore;
    private String sender;
    private List<String> tags;
    private String content;
    private boolean shouldForward;
    private Set<ClientConnection> followers;
    RegexGrammar regexGrammar = new RegexGrammar();

    public SendTask(String sender, List<String> tags, String content, boolean shouldForwrd, ConnectionStore connectionStore, UserRepo userRepo, TagRepo tagRepo) {

        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.sender = sender;
        this.tags = tags;
        this.content = content;
        this.shouldForward = shouldForwrd;
        this.connectionStore = connectionStore;
    }

    @Override
    public void executeTask() {

        String mssg = regexGrammar.buildCommand("MSG", getSender(), getContent());
        Set<String> allFollowers = getFollowers(sender);
        Set<String> localFollowers = allFollowers.stream()
                .filter(f -> !DomainUtils.isDistantDomain(f, sender))
                .collect(Collectors.toSet());

        followers = connectionStore.getConnectionsFromLogin(localFollowers);
        for (ClientConnection followerCon : followers) {
            followerCon.send(mssg);
        }


    }


    @Override
    public boolean shouldForward() {
        return shouldForward;
    }

    private Set<String> getFollowers(String sender) {
        Set<String> followers = new HashSet<>();
        String localDomain = DomainUtils.extractDomain(sender);

        List<User> users = userRepo.getAllUsers().users();
        for (User user : users) {
            if (user.getSubscribers().contains(sender)) {
                String login = user.getLogin();
                String fullLogin = login.contains("@")
                        ? login
                        : login + "@" + localDomain;
                followers.add(fullLogin);
            }
        }

        List<Tag> tags = tagRepo.getAllTags().tags();
        for (var tag : tags) {
            if (this.tags.contains(tag.getTag())) {
                followers.addAll(tag.getSubscribers());
            }
        }

        return followers;
    }

    public Set<String> getFollowersFromOtherDomain() {
        Set<String> followers = getFollowers(sender);
        Set<String> followersFromOtherDomain = new HashSet<>();
        for (String follower : followers) {
            if (DomainUtils.isDistantDomain(follower, sender)) {
                followersFromOtherDomain.add(follower);
            }
        }
        return followersFromOtherDomain;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}

