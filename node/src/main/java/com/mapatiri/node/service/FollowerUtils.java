package com.mapatiri.node.service;

import com.mapatiri.node.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fait a l'aide de l'ia
 *
 * @author Riham
 */

public class FollowerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FollowerUtils.class);

    private FollowerUtils() {

    } // ← empêche l'instanciation

    public  String stripDomain(String login) {
        return login.contains("@") ? login.substring(0, login.indexOf('@')) : login;
    }

    public  Set<String> filterSubscribers(Set<String> subscribers, String sender, String login, boolean distant) {
        return subscribers.stream()
                .filter(f -> distant == isDistantDomain(f, sender))
                .filter(f -> !stripDomain(f).equals(login))
                .map(f -> distant ? f : stripDomain(f))
                .collect(Collectors.toSet());
    }

    public  Set<String> filterTagSubscribers(List<Tag> tags, List<String> senderTags, String sender, String login, boolean distant) {
        return tags.stream()
                .filter(tag -> senderTags.contains(tag.getTag()))
                .peek(tag -> LOGGER.trace("Tag {} matched, adding its {} subscribers",
                        tag.getTag(), tag.getSubscribers().size()))
                .flatMap(tag -> tag.getSubscribers().stream())
                .filter(f -> distant == isDistantDomain(f, sender))
                .filter(f -> !stripDomain(f).equals(login))
                .map(f -> distant ? f : stripDomain(f))
                .collect(Collectors.toSet());
    }
    public String extractDomain(String userAtDomain) {
        return userAtDomain.substring(userAtDomain.indexOf('@') + 1);
    }

    public boolean isDistantDomain(String target, String localDomain) {
        String targetDomain = target.contains("@")
                ? extractDomain(target)
                : target;
        String senderDomain = localDomain.contains("@")
                ? extractDomain(localDomain)
                : localDomain;
        return !targetDomain.equals(senderDomain);
    }

    public String getLogin(String userAtDomain) {
        if (userAtDomain == null || !userAtDomain.contains("@")) {
            throw new IllegalArgumentException("Format invalide : " + userAtDomain);
        }
        return userAtDomain.substring(0, userAtDomain.indexOf("@"));
    }
}
