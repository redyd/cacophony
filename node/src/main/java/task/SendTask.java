package task;

import com.mapatiri.grammar.RegexGrammar;
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
    private Set<ClientConnection> followers ;
    private TaskRepeater taskRepeater = new TaskRepeater();


    public SendTask(String sender, List<String> tags, String content, boolean shouldForwrd, ConnectionStore connectionStore, UserRepo userRepo, TagRepo tagRepo) {

        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.sender = sender;
        this.tags = tags;
        this.content = content;
        this.shouldForward = shouldForwrd;
        this.connectionStore = connectionStore;
    }

    @Override
    public void executeTask() {

        if (shouldForward) {
            taskRepeater.convertAndSend(this);
        }
        followers = connectionStore.getConnectionsFromLogin( getFollowers(sender));
        for (ClientConnection followerCon : followers) {
            followerCon.send(content);
        }


    }

    public Optional<ClientConnection> getFollowerCon(String login) {
        for (var follower : followers){
            if(follower.getUser().getLogin().equals(login)){
                return Optional.of(follower);
            }
        }
        return Optional.empty();
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
    public Set<String> getFollowersFromOtherDomain() {
        Set<String> followers = getFollowers(sender);
        Set<String> followersFromOtherDomain = new HashSet<>();
        for (String follower : followers) {
            if(!compareDomain(follower,sender)){
                followersFromOtherDomain.add(follower);
            }
        }
        return followersFromOtherDomain;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
    private String getDomain ( String userDomain){
        RegexGrammar regexGrammar = new RegexGrammar(userDomain);
        List<String> components = regexGrammar.parseCommand();
        return components.getFirst();
    }
    private boolean compareDomain( String userDomain1 , String userDomain2){
        return getDomain(userDomain1).equals(getDomain(userDomain2));
    }
}

