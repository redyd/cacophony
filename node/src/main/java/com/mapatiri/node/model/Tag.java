package com.mapatiri.node.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité tag.
 *
 * @author Mattéo
 */
public class Tag {

    private final String tag;
    private final String domain;
    private final Set<String> subscribers;

    public Tag(String tag, String domain) {
        this.tag = tag;
        this.subscribers = new HashSet<>();
        this.domain = domain;
    }

    public Tag() {
        this.tag = "";
        this.subscribers = new HashSet<>();
        this.domain = "";
    }

    public String getTag() { return tag; }

    public String getDomain() { return domain; }

    public Set<String> getSubscribers() { return subscribers; }

    public void addSubscriber(String subscriber) {
        subscribers.add(subscriber);
    }
}
