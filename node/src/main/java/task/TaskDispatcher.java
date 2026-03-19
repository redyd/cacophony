package task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;

import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.AuthTaskManager;
import com.mapatiri.node.service.ConnectionStore;

import javax.net.ssl.SSLSocket;
import java.util.List;

public class TaskDispatcher {


    private RegexGrammar regexGrammar;
    private RequestTaskManager requestTaskManager;
    private UserRepo userRepo;
    private TagRepo tagRepo;
    private ConnectionStore connectionStore;
    private AuthTaskManager authTaskManager;


    public TaskDispatcher(RequestTaskManager requestTaskManager, UserRepo userRepo, TagRepo tagRepo, ConnectionStore connectionStore, AuthTaskManager authTaskManager) {
        this.requestTaskManager = requestTaskManager;
        regexGrammar = new RegexGrammar();
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.authTaskManager = authTaskManager;
    }

    public ClientConnection startAuth(SSLSocket sslSocket) throws AuthentificationFailed{
        //TODO à implementer

        return null;
    }
    public void submit(String request, String senderDomain) throws BadRequestException {
        regexGrammar = new RegexGrammar(request);
        String type = regexGrammar.getTypeOfCommand();
        if (type.equalsIgnoreCase("SUBSCRIBE")) {
            List<String> composents = regexGrammar.parseCommand();
            SubscribeTask subscribeTask = new SubscribeTask(userRepo, tagRepo, senderDomain, composents.getFirst(), true);
            requestTaskManager.submit(subscribeTask);
        } else if (type.equalsIgnoreCase("MSG")) {
            List<String> composents = regexGrammar.parseCommand();
            List<String> tags = regexGrammar.extractTagsFromMessage();
            SendTask sendTask = new SendTask(senderDomain, tags, composents.get(1), true, connectionStore, userRepo, tagRepo);
            requestTaskManager.submit(sendTask);
        } else {
            throw new BadRequestException("The request should only be MSG or SUBSCRIBE");
        }

    }
}
