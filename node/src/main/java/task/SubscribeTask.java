package task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.NodeConfigSaveException;
import com.mapatiri.node.model.Tag;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubscribeTask implements ActionTask {

    private UserRepo userRepo;
    private TagRepo tagRepo;
    private String sender;
    private String toSubscribe;

    private boolean shouldForward;
    private final static Logger LOGGER = Logger.getLogger(SubscribeTask.class.getName());

    public SubscribeTask(UserRepo repo, TagRepo tagRepo, String sender, String toSubscribe, boolean shouldForward) {
        this.userRepo = repo;
        this.tagRepo = tagRepo;
        this.sender = sender;
        this.toSubscribe = toSubscribe;
        this.shouldForward = shouldForward;

    }

    @Override
    public void executeTask() {
        try {

            if (toSubscribe.startsWith("#")) {
                tagRepo.addTag(new Tag(toSubscribe));
                tagRepo.addSubscriberToTag(toSubscribe, sender);
                userRepo.addUserTag(sender, toSubscribe);
            } else {
                userRepo.addSubscriberToUser(sender, toSubscribe);
            }


        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to add subscriber to tag", e);
        }
    }

    @Override
    public boolean shouldForward() {
        return shouldForward;
    }

    public String getSender() {
        return sender;
    }

    public String getToSubscribe() {
        return toSubscribe;
    }


}
