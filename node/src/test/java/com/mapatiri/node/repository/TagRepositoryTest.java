package com.mapatiri.node.repository;

import com.mapatiri.node.model.Tag;
import com.mapatiri.node.model.Tags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TagRepositoryTest {

    private TagRepository repo;

    @BeforeEach
    void setUp() {
        repo = new TagRepository();
    }

    @Test
    void AddTagAndRetrieve() throws IOException {
        repo.addTag(new Tag("#monTag1"));
        Tags all = repo.getAllTags();
        assertNotNull(all);
        assertEquals(1, all.tags().size());
        assertEquals("#monTag1", all.tags().get(0).getTag());
    }

    @Test
    void AddSubscriber(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("tags.json");
        Files.writeString(path, "{\"tags\":[]}");
        repo.loadTags(path);

        Tag tag = new Tag("#monTag1");
        repo.addTag(tag);

        repo.addSubscriberToTag("#monTag1", "louis@node1.swilabus.xyz");
        repo.addSubscriberToTag("#monTag1", "godswila@node1.swilabus.xyz");

        Tag loaded = repo.getAllTags().tags().get(0);
        assertTrue(loaded.getSubscribers().contains("louis@node1.swilabus.xyz"));
        assertTrue(loaded.getSubscribers().contains("godswila@node1.swilabus.xyz"));
    }

    @Test
    void SaveAndLoadTags(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("tags-filled.json");
        Files.writeString(path, "{\"tags\":[]}");
        repo.loadTags(path);

        Tag tag = new Tag("#monTag1");
        tag.addSubscriber("louis@node1.swilabus.xyz");
        repo.addTag(tag);
        repo.saveTags();

        TagRepository newRepo = new TagRepository();
        newRepo.loadTags(path);

        Tag loaded = newRepo.getAllTags().tags().get(0);
        assertEquals("#monTag1", loaded.getTag());
        assertTrue(loaded.getSubscribers().contains("louis@node1.swilabus.xyz"));
    }

    @Test
    void LoadModifySaveTagJson() throws Exception {
        Path path = Path.of("src/test/resources/tag-test.json");

        TagRepository repo = new TagRepository();
        repo.loadTags(path);

        repo.addTag(new Tag("#tagTest"));
        repo.addSubscriberToTag("#tagTest", "testUser@node1.swilabus.xyz");

        repo.saveTags();
    }
}