package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;

import java.util.List;

public class TaskConverter {

    // private final RequestTaskManager requestTaskManager;

    public TaskConverter(/* RequestTaskManager requestTaskManager */) {
        // this.requestTaskManager =  requestTaskManager;
    }

    /**
     * Converti un message POST en ActionTask et ajoute la tache a la pile
     *
     * @param message le message a convertir et a envoyer
     */
    public void convertAndSend(String message) {
        RegexGrammar grammar = new RegexGrammar(message);
        List<String> parts = grammar.parseCommand();

        String innerMessage = parts.get(3);
        RegexGrammar innerGrammar = new RegexGrammar(innerMessage);
        String innerType = innerGrammar.getTypeOfCommand();

        ActionTask task = switch (innerType) {
            // TODO: rajouter le flag pour le should forward a false dans les task
            case "MSG" -> new SendTask();
            case "SUBSCRIBE" -> new SubscribeTask();
            default -> throw new IllegalArgumentException("Type inconnu : " + innerType);
        };

        // requestTaskManager.submitTask(task);

    }
}
