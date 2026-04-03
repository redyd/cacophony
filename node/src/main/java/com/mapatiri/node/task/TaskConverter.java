package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.Msg;
import com.mapatiri.grammar.parse.ParsedCommand;
import com.mapatiri.grammar.parse.Post;
import com.mapatiri.grammar.parse.Subscribe;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Convertit un message POST pour la mettre sur la pile des tâches.
 *
 * @author Mattéo
 */
public class TaskConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskConverter.class);

    private final RequestTaskManager requestTaskManager;
    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final ConnectionStore connectionStore;
    private final ConfigRepository configRepository;
    private final IdentityUtils identityUtils;

    @Inject
    public TaskConverter(RequestTaskManager requestTaskManager, UserRepo userRepo, TagRepo tagRepo, ConnectionStore connectionStore, ConfigRepository configRepository, IdentityUtils identityUtils) {
        this.requestTaskManager = requestTaskManager;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.configRepository = configRepository;
        this.identityUtils = identityUtils;
    }

    /**
     * Converti un message POST en ActionTask et ajoute la tache a la pile
     *
     * @param message le message a convertir et a envoyer
     */
    public void convertAndSend(String message) {
        RegexGrammar grammar = new RegexGrammar(message);
        Post post = switch (grammar.parse(message)) {
            case Post p -> p;
            default -> throw new IllegalArgumentException("Expected POST command: " + message);
        };

        String idAtDomain = post.id();
        String sender = post.sender();
        String receiver = post.receiver();
        String innerMessage = post.content();
        String userLogin = identityUtils.stripDomain(sender);
        Optional<ClientConnection> maybeClient = connectionStore.get(userLogin);

        try {
            ActionTask task = getActionTask(innerMessage, sender, receiver);

            maybeClient.ifPresent(client -> {
                if (client.hasAlreadyReceived(idAtDomain)) {
                    LOGGER.info("A duplicate message was about to be sent to {}", client.getUser().getLogin());
                    return;
                }

                client.markAsReceived(idAtDomain);
            });

            requestTaskManager.submit(task);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown message type received. It will be ignored.");
        }
    }

    private @NonNull ActionTask getActionTask(String innerMessage, String sender, String receiver) {
        RegexGrammar grammar = new RegexGrammar();


        return switch (grammar.parse(innerMessage)) {
            case Msg msg -> {

                List<String> tags = grammar.extractTagsFromMessage(innerMessage);

                yield new SendTask(sender, tags, msg.content(), false,
                        connectionStore, userRepo, tagRepo, configRepository, identityUtils);
            }
            case Subscribe ignored -> new SubscribeTask(userRepo, tagRepo, identityUtils, sender, receiver, false);
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }
}
