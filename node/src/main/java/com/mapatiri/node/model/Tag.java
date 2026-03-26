package com.mapatiri.node.model;

import com.mapatiri.node.task.SubscribeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Tag {
    private String tag;
    private Set<String> subscribers;
    private final static Logger LOGGER = LoggerFactory.getLogger(Tag.class);

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
        for(String sub :  subscribers) {
            LOGGER.debug("The subscriber added " + subscriber + " ALL OF THE SUBSCRIBER OF THE TAG " + sub);
        }
    }
}
