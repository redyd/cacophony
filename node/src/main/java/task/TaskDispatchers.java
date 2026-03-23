package task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;

import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.AuthTaskManager;
import com.mapatiri.node.service.ConnectionStore;
import util.DomainUtils;

import javax.net.ssl.SSLSocket;
import java.util.List;

public class TaskDispatchers {


    private RegexGrammar regexGrammar;
    private RequestTaskManager requestTaskManager;
    private UserRepo userRepo;
    private TagRepo tagRepo;
    private ConnectionStore connectionStore;
    private AuthTaskManager authTaskManager;


    public TaskDispatchers(RequestTaskManager requestTaskManager, UserRepo userRepo, TagRepo tagRepo, ConnectionStore connectionStore, AuthTaskManager authTaskManager) {
        this.requestTaskManager = requestTaskManager;
        regexGrammar = new RegexGrammar();
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.authTaskManager = authTaskManager;
    }

    public ClientConnection startAuth(SSLSocket sslSocket) throws AuthentificationFailed {
        //TODO à implementer

        return null;
    }

    public void submit(String request, String senderDomain) throws BadRequestException {
        regexGrammar = new RegexGrammar(request);
        String type = regexGrammar.getTypeOfCommand();
        if (type.equalsIgnoreCase("SUBSCRIBE")) {
            List<String> composents = regexGrammar.parseCommand();
            boolean shouldForward = DomainUtils.isDistantDomain(composents.getFirst(), senderDomain);
            SubscribeTask subscribeTask = new SubscribeTask(userRepo, tagRepo, senderDomain, composents.getFirst(), shouldForward);
            requestTaskManager.submit(subscribeTask);
        } else if (type.equalsIgnoreCase("MSG")) {
            List<String> composents = regexGrammar.parseCommand();
            List<String> tags = regexGrammar.extractTagsFromMessage();
            SendTask sendTask = new SendTask(composents.get(0), tags, composents.get(1), false, connectionStore, userRepo, tagRepo);
            requestTaskManager.submit(sendTask);
        } else if (type.equalsIgnoreCase("SEND")) {
            List<String> composents = regexGrammar.parseCommand();
            List<String> tags = regexGrammar.extractTagsFromMessage();
            //should forward est toujour true car ici on peut pas comparer les domain mais on doit aller chercher dans le json si il ya des abbone externe
            // dans dautre node a ce mssg ou ce user pour definir le shouldforward a true ou false (cette verification ce fait dans le taskreapeter)
            SendTask sendTask = new SendTask(senderDomain, tags, composents.getFirst(), true, connectionStore, userRepo, tagRepo);
            requestTaskManager.submit(sendTask);

        } else {
            throw new BadRequestException("The request should only be SEND, MSG or SUBSCRIBE");
        }
    }

}
