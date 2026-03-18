package task;

import com.mapatiri.node.model.*;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;

import java.util.*;

public class SendTask implements ActionTask {

    private UserRepo userRepo;
    private TagRepo tagRepo;
    private ConnectionStore connectionStore;
    private String sender;
    private List<String> tags;
    private String content;
    private boolean shouldForward;
    private NodeConfig nodeConfig;

    public SendTask(String sender, List<String> tags, String content, boolean shouldForwrd, NodeConfig nodeConfig, ConnectionStore connectionStore, UserRepo userRepo, TagRepo tagRepo) {

        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.sender = sender;
        this.tags = tags;
        this.content = content;
        this.shouldForward = shouldForwrd;
        this.nodeConfig = nodeConfig;
        this.connectionStore = connectionStore;
    }

    @Override
    public void executeTask() {
        Set<String> followers = getFollowers(sender);
        for (String followerLogin : followers) {
            Optional<ClientConnection> connection = connectionStore.get(followerLogin);
            connection.ifPresent(con -> con.send(content));
        }
    }

    @Override
    public boolean shouldForward() {
        return shouldForward;
    }

    private Set<String> getFollowers(String sender) {
        Set<String> followers = new HashSet<>();
        List<User> users = userRepo.getAllUsers().users();
        for (User user : users) {
            if (user.getSubscribers().contains(sender)) {
                followers.add(user.getLogin());
            }
        }
        List<Tag> tags = tagRepo.getAllTags().tags();
        for (var tag : tags) {
           if(this.tags.contains(tag.getTag())){
               followers.addAll(tag.getSubscribers());
           }
        }

        return followers;

    }

}

