package com.mapatiri.node.repository;

import com.mapatiri.node.exception.UserConflictException;
import com.mapatiri.node.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository repo;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
    }

    @Test
    void testAddUserAndRetrieve(@TempDir Path tempDir) throws IOException, UserConflictException {
        Path path = tempDir.resolve("users.json");
        Files.writeString(path, "{\"users\":[]}");
        repo.loadUsers(path);

        repo.addUser(new User("louis", "hash123", 14, "saltXYZ"));
        User user = repo.getUserFromLogin("louis");

        assertNotNull(user);
        assertEquals("louis", user.getLogin());
        assertEquals("hash123", user.getBcryptHash());
        assertEquals(14, user.getBcryptRound());
        assertEquals("saltXYZ", user.getBcryptSalt());
        assertTrue(user.getSubscribers().isEmpty());
        assertTrue(user.getUserTags().isEmpty());
    }

    @Test
    void testAddSubscriber(@TempDir Path tempDir) throws IOException, UserConflictException {
        Path path = tempDir.resolve("users.json");
        Files.writeString(path, "{\"users\":[]}");
        repo.loadUsers(path);

        repo.addUser(new User("godswila", "hash123", 14, "saltXYZ"));
        repo.addSubscriberToUser("godswila", "louis");

        User user = repo.getUserFromLogin("godswila");
        assertTrue(user.getSubscribers().contains("louis"));
    }

    @Test
    void testAddUserTag(@TempDir Path tempDir) throws IOException, UserConflictException {
        Path path = tempDir.resolve("users.json");
        Files.writeString(path, "{\"users\":[]}");
        repo.loadUsers(path);

        repo.addUser(new User("louis", "hash123", 14, "saltXYZ"));
        repo.addUserTag("louis", "#tagDeTest");

        User user = repo.getUserFromLogin("louis");
        assertTrue(user.getUserTags().contains("#tagDeTest"));
    }

    @Test
    void testSaveAndLoadUsers(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("users-test.json");
        Files.writeString(path, "{\"users\":[]}");
        repo.loadUsers(path);

        repo.addUser(new User("louis", "hash123", 14, "saltXYZ"));
        repo.addUserTag("louis", "#tag1");
        repo.saveUsers();

        UserRepository newRepo = new UserRepository();
        newRepo.loadUsers(path);

        User loaded = newRepo.getUserFromLogin("louis");
        assertNotNull(loaded);
        assertEquals("hash123", loaded.getBcryptHash());
        assertEquals(14, loaded.getBcryptRound());
        assertEquals("saltXYZ", loaded.getBcryptSalt());
        assertTrue(loaded.getUserTags().contains("#tag1"));
    }

    @Test
    void testLoadModifySaveUserJson(@TempDir Path temp) throws Exception {
        Path tempTarget = temp.resolve("users-test.json");
        Path path = Path.of("src/test/resources/user-test.json");
        Files.copy(path, tempTarget);

        UserRepository repo = new UserRepository();
        repo.loadUsers(tempTarget);

        repo.addUser(new User("testUser", "hash123", 14, "saltXYZ"));
        repo.addUserTag("testUser", "#tagTest");
        repo.addSubscriberToUser("testUser", "louis");

        repo.saveUsers();
    }
}