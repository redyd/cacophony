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
    private final ConnectionStore connectionStore;
    private final ConfigRepository configRepository;

    private RegexGrammar regexGrammar;

    @Inject
    public TaskDispatcher(
            RequestTaskManager requestTaskManager,
            UserRepo userRepo,
            TagRepo tagRepo,
            ConnectionStore connectionStore,
            AuthTaskManager auth,
            ConfigRepository configRepository) {

        this.auth = auth;
        this.requestTaskManager = requestTaskManager;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.configRepository = configRepository;
    }

    @Override
    public ClientConnection startAuth(Socket socket) throws AuthentificationFailed {
        LOGGER.info("Starting auth for socket '{}'", socket.getRemoteSocketAddress());

        try {
            ClientConnection connection = auth.startHandshake(socket);
            LOGGER.info("Auth completed successfully for socket '{}'", socket.getRemoteSocketAddress());
            return connection;
        } catch (AuthentificationFailed e) {
            LOGGER.error("Auth failed for socket '{}'", socket.getRemoteSocketAddress(), e);
            throw e;
        }
    }


    @Override
    public void submit(String request, String login) throws BadRequestException {
        String sender = configRepository.buildAtDomain(login);
        LOGGER.info("Processing request from '{}', of type '{}'", login, getRequestPreview(request));

        regexGrammar = new RegexGrammar(request);
        String type = regexGrammar.getTypeOfCommand();
        String successMessage;

        ActionTask task = switch (type.toUpperCase()) {
            case "SUBSCRIBE" -> {
                successMessage = "Subscribed with success";
                yield buildSubscribeTask(sender);
            }
            case "SEND" -> {
                successMessage = "Sent with success";
                yield buildSendTask(login, true);
            }
            case "MSG" -> {
                successMessage = "Received with success";
                yield buildSendTask(regexGrammar.parseCommand().getFirst(), false);
            }
            default -> {
                LOGGER.warn("User {} made a bad request: '{}'", type, login);
                throw new BadRequestException("The request should only be SEND, MSG or SUBSCRIBE");
            }
        };

        String finalSuccessMessage = successMessage;

        requestTaskManager.submit(task);
        connectionStore.get(login).ifPresent(connection -> {
           String ok = new RegexGrammar().buildCommand("OK", finalSuccessMessage);
           connection.send(ok);
        });
    }

    private SubscribeTask buildSubscribeTask(String sender) {
        List<String> components = regexGrammar.parseCommand();
        String target = components.getFirst();
        boolean shouldForward = DomainUtils.isDistantDomain(target, sender);

        return new SubscribeTask(userRepo, tagRepo, sender, target, shouldForward);
    }

    private SendTask buildSendTask(String sender, boolean shouldForward) {
        List<String> components = regexGrammar.parseCommand();
        List<String> tags = regexGrammar.extractTagsFromMessage();
        String content = shouldForward ? components.getFirst() : components.get(1);

        return new SendTask(sender, tags, content, shouldForward, connectionStore, userRepo, tagRepo, configRepository);
    }

    private String getRequestPreview(String request) {
        if (request.length() > 50) {
            return request.substring(0, 50) + "...";
        }
        return request;
    }
}
