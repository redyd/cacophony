package com.mapatiri.node.controller;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.AuthTaskManager;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.task.ActionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mapatiri.node.task.RequestTaskManager;
import com.mapatiri.node.task.SendTask;
import com.mapatiri.node.task.SubscribeTask;
import com.mapatiri.node.util.DomainUtils;

import java.net.Socket;
import java.util.List;

public class TaskDispatcher implements Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(TaskDispatcher.class);

    private final AuthTaskManager auth;
    private RequestTaskManager requestTaskManager;
    private UserRepo userRepo;
    private TagRepo tagRepo;
    private ConnectionStore connectionStore;
    private RegexGrammar regexGrammar;
    private ConfigRepository  configRepository;

    @Inject
    public TaskDispatcher(RequestTaskManager requestTaskManager, UserRepo userRepo, TagRepo tagRepo,
                          ConnectionStore connectionStore, AuthTaskManager auth, ConfigRepository configRepository) {
        this.auth = auth;
        this.requestTaskManager = requestTaskManager;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.configRepository = configRepository;

        logger.info("TaskDispatcher initialized");
    }

    @Override
    public ClientConnection startAuth(Socket socket) throws AuthentificationFailed {
        logger.info("Starting auth handshake for socket={}", socket.getRemoteSocketAddress());
        try {
            ClientConnection connection = auth.startHandshake(socket);
            logger.info("Auth handshake completed successfully for socket={}", socket.getRemoteSocketAddress());
            return connection;
        } catch (AuthentificationFailed e) {
            logger.error("Auth handshake failed for socket={}", socket.getRemoteSocketAddress(), e);
            throw e;
        }
    }


    @Override
    public void submit(String request, String senderDomain) throws BadRequestException {
        String sender =  senderDomain + "@" + configRepository.getConfig().getCurrentDomain();
        logger.info("Processing submitted request from senderDomain={}, requestType={}",
                senderDomain, getRequestPreview(request));

        regexGrammar = new RegexGrammar(request);
        String type = regexGrammar.getTypeOfCommand();
        logger.debug("Parsed command type={} from request", type);

        ActionTask task = switch (type.toUpperCase()) {
            case "SUBSCRIBE" -> buildSubscribeTask(sender);
            case "SEND"      -> buildSendTask(senderDomain, true);
            case "MSG"       -> buildSendTask(regexGrammar.parseCommand().get(0), false);
            default          -> {
                logger.warn("Invalid command type={} from senderDomain={}", type, senderDomain);
                throw new BadRequestException("The request should only be SEND, MSG or SUBSCRIBE");
            }
        };

        requestTaskManager.submit(task);
        logger.debug("Request processing completed for senderDomain={}", senderDomain);
    }

    private SubscribeTask buildSubscribeTask(String sender) {
        List<String> components = regexGrammar.parseCommand();
        String target = components.getFirst();
        boolean shouldForward = DomainUtils.isDistantDomain(target, sender);
        logger.info("SUBSCRIBE: sender={} -> target={}, shouldForward={}", sender, target, shouldForward);

        SubscribeTask task = new SubscribeTask(userRepo, tagRepo, sender, target, shouldForward);
        logger.info("Submitted SubscribeTask: sender={} -> target={}", sender, target);
        return task;
    }

    private SendTask buildSendTask(String sender, boolean shouldForward) {
        List<String> components = regexGrammar.parseCommand();
        List<String> tags = regexGrammar.extractTagsFromMessage();
        String content = shouldForward ? components.getFirst() : components.get(1);
        logger.info("{}: sender={}, tags={}, contentLength={}",
                shouldForward ? "SEND" : "MSG", sender, tags, content.length());

        SendTask task = new SendTask(sender, tags, content, shouldForward, connectionStore, userRepo, tagRepo, configRepository);
        logger.info("Submitted {} SendTask: sender={}", shouldForward ? "forwardable SEND" : "local MSG", sender);
        return task;
    }

   private String getRequestPreview(String request) {
        if (request.length() > 50) {
            return request.substring(0, 50) + "...";
        }
        return request;
    }
}
