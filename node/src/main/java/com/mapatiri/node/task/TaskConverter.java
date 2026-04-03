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

        RegexGrammar innerGrammar = new RegexGrammar(innerMessage);
        String innerType = innerGrammar.getTypeOfCommand();

        if (innerType.equals("MSG")) {
            List<String> innerParts = innerGrammar.parseCommand();
            String content = innerParts.get(1);
            String receiverLogin = identityUtils.stripDomain(receiver);

            connectionStore.get(receiverLogin).ifPresent(client -> {
                try {
                    if (!client.hasAlreadyReceived(idAtDomain)) {
                        client.markAsReceived(idAtDomain);
                        String msg = new RegexGrammar().buildCommand("MSG", sender, content);
                        client.send(msg);
                        LOGGER.info("Message of {} delivered to {}", sender, receiverLogin);
                    } else {
                        LOGGER.info("Duplicated ignored for {}", receiverLogin);
                    }
                } catch (Exception e) {
                    LOGGER.error("Delivery error to {}", receiverLogin, e);
                }
            });

        } else if (innerType.equals("SUBSCRIBE")) {
            ActionTask task = new SubscribeTask(userRepo, tagRepo, identityUtils, sender, receiver, false);
            requestTaskManager.submit(task);
        } else {
            LOGGER.warn("Unknown type in POST : {}", innerType);
        }
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
            default -> throw new IllegalArgumentException("Unknown type : " + innerType);
        };
    }
}
