package com.mapatiri.node.repository;

import com.mapatiri.node.exception.TagConfigException;
import com.mapatiri.node.model.Tag;
import com.mapatiri.node.model.Tags;

import java.io.IOException;
import java.nio.file.Path;

public interface TagRepo {

    void loadTags(Path path) throws TagConfigException;

    void saveTags(Path path) throws IOException;

    void addTag(Tag tag);

    void addSubscriberToTag(String tagName, String subscriber);

    Tags getAllTags();
}
