package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.FollowerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TaskConverter {

    private static final Logger logger = LoggerFactory.getLogger(TaskConverter.class);

    private final RequestTaskManager requestTaskManager;
    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final ConnectionStore connectionStore;
    private final ConfigRepository configRepository;

    @Inject
    public TaskConverter(RequestTaskManager requestTaskManager, UserRepo userRepo, TagRepo tagRepo, ConnectionStore connectionStore, ConfigRepository configRepository ) {
        this.requestTaskManager =  requestTaskManager;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.configRepository = configRepository;
    }

    /**
     * Converti un message POST en ActionTask et ajoute la tache a la pile
     *
     * @param message le message a convertir et a envoyer
     */
    public void convertAndSend(String message) {
        logger.info("Conversion du message POST : {}", message);
        RegexGrammar grammar = new RegexGrammar(message);
        List<String> parts = grammar.parseCommand();

        String idAtDomain = parts.get(0);
        String sender = parts.get(1);
        String receiver = parts.get(2);
        String innerMessage = parts.get(3);
        String userLogin = FollowerUtils.stripDomain(sender);
        Optional<ClientConnection> optionalClientConnection = connectionStore.get(userLogin);
        ClientConnection clientConnection ;
        if (optionalClientConnection.isPresent()) {
            clientConnection = optionalClientConnection.get();
        }else {
            logger.info("The client connection is null");
            return;
        }

        if (clientConnection.hasAlreadyReceived(idAtDomain)) {
            logger.info("The task has already been received");
            return;
        } else {
            clientConnection.markAsReceived(idAtDomain);
        }

        logger.info("Message parse - sender: {}, receiver: {}, innerMessage: {}", sender, receiver, innerMessage);

        RegexGrammar innerGrammar = new RegexGrammar(innerMessage);
        String innerType = innerGrammar.getTypeOfCommand();

        logger.info("Type de message interne detecte : {}", innerType);

        ActionTask task = switch (innerType) {
            case "MSG" -> {
                List<String> innerParts = innerGrammar.parseCommand();
                String content = innerParts.get(1);
                List<String> tags = innerGrammar.extractTagsFromMessage();

                logger.info("Creation SendTask - sender: {}, tags: {}, content: {}", sender, tags, content);

                yield new SendTask(sender, tags, content, false,
                        connectionStore, userRepo, tagRepo, configRepository);
            }
            case "SUBSCRIBE" -> {
                logger.info("Creation SubscribeTask - sender: {}, receiver: {}", sender, receiver);
                yield new SubscribeTask(userRepo, tagRepo, sender, receiver, false);
            }
            default -> {
                logger.error("Type de message inconnu : {}", innerType);
                throw new IllegalArgumentException("Type inconnu : " + innerType);
            }
        };

        logger.info("Ajout de la tâche {} a la file des taches", task.getClass().getSimpleName());
        requestTaskManager.submit(task);
    }
}
