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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mapatiri.node.task.RequestTaskManager;
import com.mapatiri.node.task.SendTask;
import com.mapatiri.node.task.SubscribeTask;
import util.DomainUtils;

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
        logger.info("Processing submitted request from senderDomain={}, requestType={}",
                senderDomain, getRequestPreview(request));

        regexGrammar = new RegexGrammar(request);
        String type = regexGrammar.getTypeOfCommand();
        logger.debug("Parsed command type={} from request", type);

        if (type.equalsIgnoreCase("SUBSCRIBE")) {
            List<String> components = regexGrammar.parseCommand();
            String target = components.getFirst();
            boolean shouldForward = DomainUtils.isDistantDomain(target, senderDomain);
            logger.info("SUBSCRIBE: senderDomain={} -> target={}, shouldForward={}",
                    senderDomain, target, shouldForward);

            SubscribeTask subscribeTask = new SubscribeTask(userRepo, tagRepo, senderDomain, target, shouldForward);
            requestTaskManager.submit(subscribeTask);
            logger.info("Submitted SubscribeTask: senderDomain={} -> target={}", senderDomain, target);

        } else if (type.equalsIgnoreCase("MSG")) {
            List<String> components = regexGrammar.parseCommand();
            List<String> tags = regexGrammar.extractTagsFromMessage();
            String sender = components.get(0);
            String content = components.get(1);
            logger.info("MSG: senderDomain={}, sender={}, tags={}, contentLength={}",
                    senderDomain, sender, tags, content.length());

            SendTask sendTask = new SendTask(sender, tags, content, false, connectionStore, userRepo, tagRepo, configRepository);
            requestTaskManager.submit(sendTask);
            logger.info("Submitted local MSG SendTask: senderDomain={}", senderDomain);

        } else if (type.equalsIgnoreCase("SEND")) {
            List<String> components = regexGrammar.parseCommand();
            List<String> tags = regexGrammar.extractTagsFromMessage();
            String content = components.getFirst();
            logger.info("SEND: senderDomain={}, tags={}, contentLength={}",
                    senderDomain, tags, content.length());

            SendTask sendTask = new SendTask(senderDomain, tags, content, true, connectionStore, userRepo, tagRepo, configRepository);
            requestTaskManager.submit(sendTask);
            logger.info("Submitted forwardable SEND SendTask: senderDomain={}", senderDomain);

        } else {
            logger.warn("Invalid command type={} from senderDomain={}", type, senderDomain);
            throw new BadRequestException("The request should only be SEND, MSG or SUBSCRIBE");
        }

        logger.debug("Request processing completed for senderDomain={}", senderDomain);
    }

   private String getRequestPreview(String request) {
        if (request.length() > 50) {
            return request.substring(0, 50) + "...";
        }
        return request;
    }
}
