package com.mapatiri.node.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapatiri.node.model.User;
import com.mapatiri.node.model.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository repo;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        repo.usersConfig = new Users(new java.util.ArrayList<>());
    }

    @Test
    void testAddUserAndRetrieve() {
        repo.addUser("louis", "hash123", 14, "saltXYZ");
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
    void testAddSubscriber() {
        repo.addUser("godswila", "hash456", 14, "saltABC");
        repo.addSubscriberToUser("godswila", "louis");

        User user = repo.getUserFromLogin("godswila");
        assertTrue(user.getSubscribers().contains("louis"));
    }

    @Test
    void testAddUserTag() {
        repo.addUser("louis", "hash123", 14, "saltXYZ");
        repo.addUserTag("louis", "#tagDeTest");

        User user = repo.getUserFromLogin("louis");
        assertTrue(user.getUserTags().contains("#tagDeTest"));
    }

    @Test
    void testSaveAndLoadUsers(@TempDir Path tempDir) throws Exception {
        repo.addUser("louis", "hash123", 14, "saltXYZ");
        repo.addUserTag("louis", "#tag1");

        Path path = tempDir.resolve("users-test.json");
        repo.saveUsers(path);

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
    void testLoadModifySaveUserJson() throws Exception {
        // Chemin vers ton fichier dans resources
        Path path = Path.of("src/test/resources/user-test.json");

        UserRepository repo = new UserRepository();
        repo.loadUsers(path);

        // Ajout d'un utilisateur pour test
        repo.addUser("testUser", "hashTest", 14, "saltTest");
        repo.addUserTag("testUser", "#tagTest");
        repo.addSubscriberToUser("testUser", "louis");

        // Sauvegarde physique pour vérifier le fichier
        Path output = Path.of("src/test/resources/user-test-filled.json");
        repo.saveUsers(output);
    }
}