package com.mapatiri.node.model;

import java.util.HashSet;
import java.util.Set;

public class Tag {
    private String tag;
    private Set<String> subscribers;

    public Tag(String tag) {
        this.tag = tag;
        this.subscribers = new HashSet<>();
    }

    public Tag() {
        this.tag = "";
        this.subscribers = new HashSet<>();
    }

    public String getTag() { return tag; }

    public Set<String> getSubscribers() { return subscribers; }

    public void addSubscriber(String subscriber) {
        subscribers.add(subscriber);
    }
}
