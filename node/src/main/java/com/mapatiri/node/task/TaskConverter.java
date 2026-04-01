package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
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
        List<String> parts = grammar.parseCommand();

        String idAtDomain = parts.get(0);
        String sender = parts.get(1);
        String receiver = parts.get(2);
        String innerMessage = parts.get(3);
        String userLogin = identityUtils.stripDomain(sender);
        Optional<ClientConnection> maybeClient = connectionStore.get(userLogin);

        maybeClient.ifPresent(client -> {
            if (client.hasAlreadyReceived(idAtDomain)) {
                LOGGER.info("A duplicate message was about to be sent to {}", client.getUser().getLogin());
                return;
            }

            client.markAsReceived(idAtDomain);
        });

        ActionTask task = getActionTask(innerMessage, sender, receiver);

        requestTaskManager.submit(task);
    }

    private @NonNull ActionTask getActionTask(String innerMessage, String sender, String receiver) {
        RegexGrammar innerGrammar = new RegexGrammar(innerMessage);
        String innerType = innerGrammar.getTypeOfCommand();

        return switch (innerType) {
            case "MSG" -> {
                List<String> innerParts = innerGrammar.parseCommand();
                String content = innerParts.get(1);
                List<String> tags = innerGrammar.extractTagsFromMessage();

                yield new SendTask(sender, tags, content, false,
                        connectionStore, userRepo, tagRepo, configRepository, identityUtils);
            }
            case "SUBSCRIBE" -> new SubscribeTask(userRepo, tagRepo, identityUtils, sender, receiver, false);
            default -> throw new IllegalArgumentException("Type inconnu : " + innerType);
        };
    }
}
