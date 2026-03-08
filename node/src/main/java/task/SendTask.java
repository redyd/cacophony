package task;

import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.service.ConfigService;
import com.mapatiri.node.service.ConnectionStore;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public class SendTask implements ActionTask {
    private ConfigService jsonService;
    private ConnectionStore connectionStore;
    private String sender;
    private List<String> tags;
    private String content;
    private boolean shouldForwrd;
    private NodeConfig nodeConfig;
    public SendTask(ConfigService jsonService, String sender, List<String> tags, String content, boolean shouldForwrd, NodeConfig nodeConfig, ConnectionStore connectionStore) {
        this.jsonService = jsonService;
        this.sender = sender;
        this.tags = tags;
        this.content = content;
        this.shouldForwrd = shouldForwrd;
        this.nodeConfig = nodeConfig;
        this.connectionStore = connectionStore;
    }

    @Override
    public void executeTask() {
        Set<String> followers = getFollowers(sender);
        followers.addAll(getTagsFollowers());
        for (String follower : followers) {

            Optional<ClientConnection> connection = connectionStore.get(follower);
            if (connection.isPresent()) {
                var con = connection.get();
                if (shouldForwrd) {
                    con.send(content);
                }
            }
        }
    }

    @Override
    public boolean shouldForward() {
        return shouldForwrd;
    }

    private Set<String> getFollowers(String sender) {
        Set<String> followers = new HashSet<>();
        Map<String, Set<String>> subscription = nodeConfig.getSubscriptions();
        for(Map.Entry<String, Set<String>> entry : subscription.entrySet()){
            if(entry.getValue().contains(sender)){
                followers.add(entry.getKey());
            }
        }
        return followers;

    }

    private Set<String> getTagsFollowers(){
        Set<String> followers = new HashSet<>();
        for(var  tag : tags){
            followers = new HashSet<>(getFollowers(tag));

        }
        return followers;
    }
}

