package task;

import com.mapatiri.node.exception.NodeConfigSaveException;
import com.mapatiri.node.service.ConfigService;
import com.mapatiri.node.service.JsonService;

import java.util.logging.Logger;

public class SubscribeTask implements ActionTask {
    private ConfigService jsonService;
    private String sender;
    private String toSubscribe;
    private boolean shouldForward;
    private final static Logger LOGGER =  Logger.getLogger(SubscribeTask.class.getName());

    public SubscribeTask(JsonService jsonService, String sender, String toSubscribe,  boolean shouldForward) {
        //faire des verifications de si cest nul ou non
        this.jsonService = jsonService;
        this.sender = sender;
        this.toSubscribe = toSubscribe;
        this.shouldForward = shouldForward;
    }

    @Override
    public void executeTask() {
        try {
            jsonService.addSubscription(sender, toSubscribe);

        } catch (NodeConfigSaveException e) {
            LOGGER.severe(e.getMessage());

        }
    }

    @Override
    public boolean shouldForward() {
        return shouldForward;
    }
}
