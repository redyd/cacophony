package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;

import java.util.List;

public class TaskConverter {

    private final RequestTaskManager requestTaskManager;
    private final UserRepo userRepo;
    private final TagRepo tagRepo;
    private final ConnectionStore connectionStore;
    private final ConfigRepository configRepository;

    public TaskConverter(RequestTaskManager requestTaskManager, UserRepo userRepo, TagRepo tagRepo, ConnectionStore connectionStore, ConfigRepository configRepository) {
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
        RegexGrammar grammar = new RegexGrammar(message);
        List<String> parts = grammar.parseCommand();

        String sender = parts.get(1);
        String receiver = parts.get(2);
        String innerMessage = parts.get(3);

        RegexGrammar innerGrammar = new RegexGrammar(innerMessage);
        String innerType = innerGrammar.getTypeOfCommand();

        ActionTask task = switch (innerType) {
            case "MSG" -> {
                List<String> innerParts = innerGrammar.parseCommand();
                String content = innerParts.get(1);
                List<String> tags = innerGrammar.extractTagsFromMessage();

                yield new SendTask(sender, tags, content, false,
                        connectionStore, userRepo, tagRepo, configRepository);
            }
            case "SUBSCRIBE" -> new SubscribeTask(userRepo, tagRepo, sender, receiver, false);
            default -> throw new IllegalArgumentException("Type inconnu : " + innerType);
        };

        requestTaskManager.submit(task);
    }
}
