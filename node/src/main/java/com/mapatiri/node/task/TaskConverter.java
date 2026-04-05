package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.Msg;
import com.mapatiri.grammar.parse.ParsedCommand;
import com.mapatiri.grammar.parse.Post;
import com.mapatiri.grammar.parse.Subscribe;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.TagRepo;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.ConnectionStore;
import com.mapatiri.node.service.IdentityUtils;
import com.mapatiri.node.service.SpeakerBridgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

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
    private final IdentityUtils identityUtils;
    private final SpeakerBridgeService speakerBridge;

    @Inject
    public TaskConverter(
            RequestTaskManager requestTaskManager,
            UserRepo userRepo,
            TagRepo tagRepo,
            ConnectionStore connectionStore,
            IdentityUtils identityUtils,
            SpeakerBridgeService speakerBridge) {
        this.requestTaskManager = requestTaskManager;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.connectionStore = connectionStore;
        this.identityUtils = identityUtils;
        this.speakerBridge = speakerBridge;
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
        ParsedCommand innerCommand;
        try {
            innerCommand = grammar.parse(innerMessage + "\r\n");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown or malformed inner command in POST, ignoring: '{}'", innerMessage);
            return;
        }

        switch (innerCommand) {
            case Msg m -> {
                if (receiver.startsWith("#")) {
                    handleTagMessage(idAtDomain, sender, receiver, m.content(), grammar);
                } else {
                    deliverToLocalUser(idAtDomain, sender, receiver, m.content(), grammar);
                }
            }
            case Subscribe ignored -> {
                ActionTask task = new SubscribeTask(userRepo, tagRepo, identityUtils, sender, receiver, false);
                requestTaskManager.submit(task);
            }
            default -> LOGGER.warn("Unknown type in POST: {}", innerMessage);
        }
    }

    private void handleTagMessage(String idAtDomain, String sender, String receiver, String content, RegexGrammar grammar) {
        String tagName = identityUtils.stripDomain(receiver);
        String tagDomain = identityUtils.extractDomain(receiver);

        Set<String> subscribers = tagRepo.getAllTags().tags().stream()
                .filter(tag -> tag.getTag().equals(tagName))
                .filter(tag -> tag.getDomain().equals(tagDomain))
                .flatMap(tag -> tag.getSubscribers().stream())
                .filter(subscriber -> !subscriber.equals(sender))
                .collect(Collectors.toSet());

        for (String subscriber : subscribers) {
            if (identityUtils.isDistantDomain(subscriber, sender)) {
                String innerMessage = grammar.buildContent(new Msg(sender, content));
                String post = grammar.build(new Post(idAtDomain, sender, subscriber, innerMessage));
                speakerBridge.forward(post);
                LOGGER.info("Message of {} forwarded from tag {} to {}", sender, receiver, subscriber);
            } else {
                deliverToLocalUser(idAtDomain, sender, subscriber, content, grammar);
            }
        }
    }

    private void deliverToLocalUser(String idAtDomain, String sender, String receiver, String content, RegexGrammar grammar) {
        String receiverLogin = identityUtils.stripDomain(receiver);
        connectionStore.get(receiverLogin).ifPresent(client -> {
            try {
                if (!client.hasAlreadyReceived(idAtDomain)) {
                    client.markAsReceived(idAtDomain);
                    String msg = grammar.build(new Msg(sender, content));
                    client.send(msg);
                    LOGGER.info("Message of {} delivered to {}", sender, receiverLogin);
                } else {
                    LOGGER.info("Duplicated ignored for {}", receiverLogin);
                }
            } catch (Exception e) {
                LOGGER.error("Delivery error to {}", receiverLogin, e);
            }
        });
    }
}
