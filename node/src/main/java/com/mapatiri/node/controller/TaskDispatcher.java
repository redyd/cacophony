package com.mapatiri.node.controller;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.AuthTaskManager;
import com.mapatiri.node.service.ConnectionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.RequestTaskManager;
import task.SendTask;
import task.SubscribeTask;
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

    @Inject
    public TaskDispatcher(RequestTaskManager requestTaskManager, UserRepo userRepo, TagRepo tagRepo, ConnectionStore connectionStore,AuthTaskManager auth) {
        this.auth = auth;
        this.requestTaskManager = requestTaskManager;
        regexGrammar = new RegexGrammar();
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
    }

    @Override
    public ClientConnection startAuth(Socket socket) throws AuthentificationFailed {
        return auth.startHandshake(socket);
    }

    @Override
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
