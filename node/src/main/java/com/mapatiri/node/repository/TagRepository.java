package com.mapatiri.node.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapatiri.node.exception.TagConfigException;
import com.mapatiri.node.model.Tag;
import com.mapatiri.node.model.Tags;
import com.mapatiri.node.model.Users;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class TagRepository implements TagRepo {

    private Tags tagsConfig;

    @Override
    public void loadTags(Path path) throws TagConfigException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try {
            Tags config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), Tags.class);
            if (config == null) {
                throw new TagConfigException("Config vide pour: " + path);
            }

            this.tagsConfig = config;
        } catch (IOException e) {
            throw new TagConfigException(e.getMessage());
        }
    }

    @Override
    public void saveTags(Path path) throws IOException {
        if (tagsConfig == null) return;
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Files.writeString(path, gson.toJson(tagsConfig), StandardCharsets.UTF_8);
    }

    @Override
    public void addTag(Tag tag) {
        if (tagsConfig == null) {
            tagsConfig = new Tags(new ArrayList<>());
        }
        tagsConfig.tags().add(tag);
    }

    @Override
    public void addSubscriberToTag(String tagName, String subscriber) {
        if (tagsConfig == null) return;
        for (Tag t : tagsConfig.tags()) {
            if (t.getTag().equals(tagName)) {
                t.addSubscriber(subscriber);
                return;
            }
        }
    }

    @Override
    public Tags getAllTags() {
        return tagsConfig;
    }
}
