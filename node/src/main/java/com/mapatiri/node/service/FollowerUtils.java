package com.mapatiri.node.service;

import com.mapatiri.node.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mapatiri.node.util.DomainUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fait a l'aide de l'ia
 */

public class FollowerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FollowerUtils.class);

    private FollowerUtils() {} // ← empêche l'instanciation

    public static String stripDomain(String login) {
        return login.contains("@") ? login.substring(0, login.indexOf('@')) : login;
    }

    public static Set<String> filterSubscribers(Set<String> subscribers, String sender, String login, boolean distant) {
        return subscribers.stream()
                .filter(f -> distant == DomainUtils.isDistantDomain(f, sender))
                .filter(f -> !stripDomain(f).equals(login))
                .map(f -> distant ? f : stripDomain(f))
                .collect(Collectors.toSet());
    }

    public static Set<String> filterTagSubscribers(List<Tag> tags, List<String> senderTags, String sender, String login, boolean distant) {
        return tags.stream()
                .filter(tag -> senderTags.contains(tag.getTag()))
                .peek(tag -> LOGGER.trace("Tag {} matched, adding its {} subscribers",
                        tag.getTag(), tag.getSubscribers().size()))
                .flatMap(tag -> tag.getSubscribers().stream())
                .filter(subscriber -> tags.stream()
                        .filter(tag -> senderTags.contains(tag.getTag()))
                        .allMatch(tag -> tag.getSubscribers().contains(subscriber)))
                .filter(f -> distant == DomainUtils.isDistantDomain(f, sender))
                .filter(f -> !stripDomain(f).equals(login))
                .map(f -> distant ? f : stripDomain(f))
                .collect(Collectors.toSet());
    }
}
