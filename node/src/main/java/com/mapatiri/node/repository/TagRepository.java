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
import java.util.ArrayList;

public class TagRepository implements TagRepo {

    private Tags tagsConfig;
    private Path path;

    @Override
    public void loadTags(Path path) throws TagConfigException {
        this.path = path;
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
    public void saveTags() throws IOException {
        if (path == null) return;
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Files.writeString(path, gson.toJson(tagsConfig), StandardCharsets.UTF_8);
    }

    @Override
    public void addTag(Tag tag) throws IOException {
        if (tagsConfig == null) {
            tagsConfig = new Tags(new ArrayList<>());
        }
        tagsConfig.tags().add(tag);
        saveTags();
    }

    @Override
    public void addSubscriberToTag(String tagName, String subscriber) throws IOException {
        if (tagsConfig == null) return;
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
       for(Tag t : tagsConfig.tags()){
           if(t.getTag().equals(tag.getTag())){
               return true;
           }
       }
       return false;
    }
}
