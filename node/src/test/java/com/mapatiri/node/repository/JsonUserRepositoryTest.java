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

class JsonUserRepositoryTest {

    private JsonUserRepository repo;

    @BeforeEach
    void setUp() {
        repo = new JsonUserRepository();
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
        // ← getUserTags() supprimé
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

    // ← testAddUserTag() supprimé

    @Test
    void testSaveAndLoadUsers(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("users-test.json");
        Files.writeString(path, "{\"users\":[]}");
        repo.loadUsers(path);

        repo.addUser(new User("louis", "hash123", 14, "saltXYZ"));

        JsonUserRepository newRepo = new JsonUserRepository();
        newRepo.loadUsers(path);

        User loaded = newRepo.getUserFromLogin("louis");
        assertNotNull(loaded);
        assertEquals("hash123", loaded.getBcryptHash());
        assertEquals(14, loaded.getBcryptRound());
        assertEquals("saltXYZ", loaded.getBcryptSalt());
        // ← getUserTags() supprimé
    }

    @Test
    void testDuplicateUserThrows(@TempDir Path tempDir) throws IOException, UserConflictException {
        Path path = tempDir.resolve("users.json");
        Files.writeString(path, "{\"users\":[]}");
        repo.loadUsers(path);

        repo.addUser(new User("louis", "hash123", 14, "saltXYZ"));

        assertThrows(UserConflictException.class, () ->
                repo.addUser(new User("louis", "hash456", 14, "saltABC")));
    }

    @Test
    void testGetUnknownUserReturnsNull(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("users.json");
        Files.writeString(path, "{\"users\":[]}");
        repo.loadUsers(path);

        assertNull(repo.getUserFromLogin("unknown"));
    }

    @Test
    void testLoadModifySaveUserJson(@TempDir Path temp) throws Exception {
        Path tempTarget = temp.resolve("users-test.json");
        Path path = Path.of("src/test/resources/user-test.json");
        Files.copy(path, tempTarget);

        JsonUserRepository repo = new JsonUserRepository();
        repo.loadUsers(tempTarget);

        repo.addUser(new User("testUser", "hash123", 14, "saltXYZ"));
        repo.addSubscriberToUser("testUser", "louis");
        // ← addUserTag() supprimé

        User loaded = repo.getUserFromLogin("testUser");
        assertNotNull(loaded);
        assertTrue(loaded.getSubscribers().contains("louis"));
    }
}