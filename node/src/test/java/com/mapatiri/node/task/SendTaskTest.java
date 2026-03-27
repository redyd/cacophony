//package com.mapatiri.node.task;
//
//
//import com.mapatiri.node.exception.NodeFileException;
//import com.mapatiri.node.model.*;
//import com.mapatiri.node.repository.ConfigRepository;
//import com.mapatiri.node.repository.TagRepo;
//import com.mapatiri.node.repository.UserRepo;
//import com.mapatiri.node.service.ConnectionStore;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.net.Socket;
//import java.nio.file.Path;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//    class SendTaskTest {
//
//        @Mock private UserRepo userRepo;
//        @Mock private TagRepo tagRepo;
//        @Mock private ConnectionStore connectionStore;
//        @Mock private ConfigRepository configRepository;
//
//        private Users users;
//        private Tags tags;
//
//        @BeforeEach
//        void setUp() throws NodeFileException {
//            MockitoAnnotations.openMocks(this);
//
//            // Setup default empty collections
//            users = new Users(new ArrayList<>());
//            tags = new Tags(new ArrayList<>());
//
//            when(userRepo.getAllUsers()).thenReturn(users);
//            when(tagRepo.getAllTags()).thenReturn(tags);
//
//            NodeConfig config = mock(NodeConfig.class);
//            when(config.getCurrentDomain()).thenReturn("node1.swilabus.xyz");
//
//            when(configRepository.getConfig()).thenReturn(config);
//        }
//
//        // ===== getFollowersFromOtherDomain =====
//
//        @Test
//        void getFollowersFromOtherDomain_noFollowers_returnsEmptySet() {
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of(), "Hey!", true,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//
//            assertTrue(task.getFollowersFromOtherDomain().isEmpty());
//        }
//
//        @Test
//        void getFollowersFromOtherDomain_onlyLocalFollowers_returnsEmptySet() {
//            User alice = new User("alice11111", "hash", 14, "salt");
//            alice.getSubscribers().add("louis@node1.xyz");
//            users.users().add(alice);
//
//            when(connectionStore.getConnectionsFromLogin(anySet()))
//                    .thenReturn(new HashSet<>());
//
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of(), "Hey!", true,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//
//            // alice is on node1 → not distant
//            assertTrue(task.getFollowersFromOtherDomain().isEmpty());
//        }
//
//        @Test
//        void getFollowersFromOtherDomain_distantFollower_returnsFollower() {
//            User louis = new User("louis", "hash", 14, "salt"); // ou le login sans domaine
//
//            User lswinnen = new User("lswinnen@node2.xyz", "hash", 14, "salt");
//            louis.getSubscribers().add("lswinnen@node2.xyz");
//            users.users().add(lswinnen);
//
//            when(userRepo.getUserFromLogin("louis")).thenReturn(louis);
//
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of(), "Hey!", true,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//
//            Set<String> distant = task.getFollowersFromOtherDomain();
//            assertTrue(distant.contains("lswinnen@node2.xyz"));
//        }
//
//        // ===== executeTask =====
//
//        @Test
//        void executeTask_localFollowerConnected_receivesMSG() throws Exception {
//            // Setup user subscribed to louis
//            User alice = mock(User.class);
//            when(alice.getSubscribers()).thenReturn(Set.of("louis@node1.xyz"));
//            when(alice.getLogin()).thenReturn("alice@node1.xyz");
//            users.users().add(alice);
//
//            // Setup ClientConnection with a real StringWriter to capture output
//            StringWriter output = new StringWriter();
//            PrintWriter pw = new PrintWriter(output);
//            Socket socket = mock(Socket.class);
//            when(socket.isClosed()).thenReturn(false);
//
//            User aliceUser = new User("alice11111", "hash", 14, "salt");
//            ClientConnection con = new ClientConnection(socket, aliceUser,
//                    new java.io.BufferedReader(new java.io.StringReader("")), pw);
//
//            when(connectionStore.getConnectionsFromLogin(anySet()))
//                    .thenReturn(Set.of(con));
//
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of(), "Hey!", false,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//
//            task.executeTask();
//
//            // MSG format: "MSG louis@node1.xyz Hey!"
//            assertTrue(output.toString().contains("MSG"));
//            assertTrue(output.toString().contains("louis@node1.xyz"));
//            assertTrue(output.toString().contains("Hey!"));
//        }
//
//        @Test
//        void executeTask_noFollowers_sendsNothing() {
//            when(connectionStore.getConnectionsFromLogin(anySet()))
//                    .thenReturn(new HashSet<>());
//
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of(), "Hey!", false,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//
//            // should not throw
//            assertDoesNotThrow(task::executeTask);
//            verify(connectionStore).getConnectionsFromLogin(anySet());
//        }
//
//        @Test
//        void executeTask_tagFollower_receivesMSG() throws Exception {
//            // Setup tag with subscriber
//            Tag metalTag = new Tag("#metal11111");
//            metalTag.addSubscriber("alice@node1.xyz");
//            tags.tags().add(metalTag);
//
//            // Setup ClientConnection
//            StringWriter output = new StringWriter();
//            PrintWriter pw = new PrintWriter(output);
//            Socket socket = mock(Socket.class);
//            User aliceUser = new User("alice11111", "hash", 14, "salt");
//            ClientConnection con = new ClientConnection(socket, aliceUser,
//                    new java.io.BufferedReader(new java.io.StringReader("")), pw);
//
//            when(connectionStore.getConnectionsFromLogin(anySet()))
//                    .thenReturn(Set.of(con));
//
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of("#metal11111"), "Hey #metal11111!", false,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//
//            task.executeTask();
//
//            assertTrue(output.toString().contains("MSG"));
//        }
//
//        // ===== shouldForward =====
//
//        @Test
//        void shouldForward_true_returnsTrue() {
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of(), "Hey!", true,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//            assertTrue(task.shouldForward());
//        }
//
//        @Test
//        void shouldForward_false_returnsFalse() {
//            SendTask task = new SendTask(
//                    "louis@node1.xyz", List.of(), "Hey!", false,
//                    connectionStore, userRepo, tagRepo, configRepository
//            );
//            assertFalse(task.shouldForward());
//        }
//    }

