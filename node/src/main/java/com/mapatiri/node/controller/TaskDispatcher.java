package com.mapatiri.node.controller;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.*;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.AuthTaskManager;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
import com.mapatiri.node.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mapatiri.node.task.RequestTaskManager;
import com.mapatiri.node.task.SendTask;
import com.mapatiri.node.task.SubscribeTask;

import java.net.Socket;
import java.util.List;

/**
 * Dispatcher de tâche qui gère la soumission des messages et une tentative d'authentification.
 *
 * @author Riham
 */
public class TaskDispatcher implements Dispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDispatcher.class);

    private final AuthTaskManager auth;
    private final RequestTaskManager requestTaskManager;
    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final IdentityUtils identityUtils;
    private final ConnectionStore connectionStore;
    private final ConfigRepository configRepository;
    private final ClientSessionEventNotifier notifier;
    private final RegexGrammar regexGrammar;

    @Inject
    public TaskDispatcher(
            RequestTaskManager requestTaskManager,
            UserRepo userRepo,
            TagRepo tagRepo,
            ConnectionStore connectionStore,
            AuthTaskManager auth,
            ConfigRepository configRepository,
            IdentityUtils identityUtils, ClientSessionEventNotifier notifier) {

        this.auth = auth;
        this.requestTaskManager = requestTaskManager;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.configRepository = configRepository;
        this.identityUtils = identityUtils;
        this.notifier = notifier;
        this.regexGrammar = new RegexGrammar();
    }

    @Override
    public ClientConnection startAuth(Socket socket) throws AuthentificationFailed {
        LOGGER.info("Starting auth for socket '{}'", socket.getRemoteSocketAddress());

        try {
            ClientConnection connection = auth.startHandshake(socket);
            LOGGER.info("Auth completed successfully for socket '{}'", socket.getRemoteSocketAddress());
            return connection;
        } catch (AuthentificationFailed e) {
            LOGGER.warn("Auth failed for socket '{}'", socket.getRemoteSocketAddress());
            throw e;
        }
    }


    @Override
    public void submit(String request, String login) throws BadRequestException {
        String sender = configRepository.buildAtDomain(login);
        LOGGER.info("Processing request from '{}', of type '{}'", login, getRequestPreview(request));

        ParsedCommand command;
        try {
            command = regexGrammar.parse(request);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("User {} made a bad request'", login);
            throw new BadRequestException("The request should only be SEND, MSG, SUBSCRIBE or LEAVE");
        }

        String successMessage;

        ActionTask task = switch (command) {
            case Subscribe sub -> {
                successMessage = "Subscribed with success";
                yield buildSubscribeTask(sender, sub);
            }
            case Send send -> {
                successMessage = "Sent with success";
                yield buildSendTask(login, send);
            }
            case Msg msg -> {
                successMessage = "Received with success";
                yield buildSendTask(msg);
            }
            case Leave ignored -> {
                successMessage = "Leaving with success";
                yield buildLeaveTask(login);
            }
            default -> {
                LOGGER.warn("User {} made a bad request: '{}'", command.getClass(), login);
                throw new BadRequestException("The request should only be SEND, MSG ,  SUBSCRIBE or LEAVE");
            }
        };

        String finalSuccessMessage = successMessage;

        requestTaskManager.submit(task);
        connectionStore.get(login).ifPresent(connection -> {
            String ok = regexGrammar.build(new Ok(finalSuccessMessage));
            connection.send(ok);
        });
    }

    private SubscribeTask buildSubscribeTask(String sender, Subscribe sub) {
        boolean shouldForward = identityUtils.isDistantDomain(sub.target(), sender);
        return new SubscribeTask(userRepo, tagRepo, identityUtils, sender, sub.target(), shouldForward);
    }

    private LeaveTask buildLeaveTask(String sender) {
        return new LeaveTask(notifier, connectionStore, sender);
    }

    private SendTask buildSendTask(String sender, Send send) {
        List<String> tags = regexGrammar.extractTagsFromMessage(send.content());
        return new SendTask(sender, tags, send.content(), true,
                connectionStore, userRepo, tagRepo, configRepository, identityUtils);
    }

    private SendTask buildSendTask(Msg msg) {
        List<String> tags = regexGrammar.extractTagsFromMessage(msg.content());
        return new SendTask(msg.sender(), tags, msg.content(), false,
                connectionStore, userRepo, tagRepo, configRepository, identityUtils);
    }

    private String getRequestPreview(String request) {
        if (request.length() > 50) {
            return request.substring(0, 50) + "...";
        }
        return request;
    }
}
