package com.mapatiri.node.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapatiri.node.exception.TagConfigException;
import com.mapatiri.node.model.Tag;
import com.mapatiri.node.model.Tags;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Repository de tags json.
 *
 * @author Mattéo
 */
public class JsonTagRepository implements TagRepo {

    private final Gson gson;

    private Tags tagsConfig;
    private Path path;

    public JsonTagRepository() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    public void loadTags(Path path) throws TagConfigException {
        this.path = path;

        try {
            Tags config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), Tags.class);
            if (config == null) {
                throw new TagConfigException("Empty config for: " + path);
            }

            this.tagsConfig = config;
        } catch (IOException e) {
            throw new TagConfigException(e.getMessage());
        }
    }

    @Override
    public void addTag(Tag tag) throws IOException {
        if (tagsConfig == null) {
            throw new IllegalStateException("Repository must be initialized before adding tags.");
        }

        tagsConfig.tags().add(tag);
        saveTags();
    }

    @Override
    public void addSubscriberToTag(String tagName, String subscriber) throws IOException {
        if (tagsConfig == null) {
            throw new IllegalStateException("Repository must be initialized before adding subscriber to tags.");
        }

        for (Tag t : tagsConfig.tags()) {
            if (t.getTag().equals(tagName)) {
                t.addSubscriber(subscriber);
                saveTags();
                return;
            }
        }
    }

    @Override
    public Tags getAllTags() {
        return tagsConfig;
    }

    @Override
    public boolean tagExist(Tag tag) {
        for (Tag t : tagsConfig.tags()) {
            if (t.getTag().equals(tag.getTag())) {
                return true;
            }
        }
        return false;
    }

    private void saveTags() throws IOException {
        if (path == null) {
            throw new IllegalStateException("Repository must be initialized before saving tags.");
        }

        synchronized (this) {
            Files.writeString(path, gson.toJson(tagsConfig), StandardCharsets.UTF_8);
        }
    }
//    @Override
//    public boolean tagExistInDomain(String tagName, String domain) {
//        for (Tag t : tagsConfig.tags()) {
//            if (t.getTag().equals(tagName) && t.getDomain().equals(domain)) {
//                return true;
//            }
//        }
//        return false;
//    }
}
