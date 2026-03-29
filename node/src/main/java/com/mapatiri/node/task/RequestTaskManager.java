package com.mapatiri.node.task;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * File de tâche qui va exécuter les différentes requêtes d'envois de messages et d'abonnements (tâches {@link ActionTask)}.
 *
 * @author Riham
 */
public class RequestTaskManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestTaskManager.class);

    private final TaskRepeater taskRepeater;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public RequestTaskManager(TaskRepeater taskRepeater) {
        this.taskRepeater = taskRepeater;
    }

    /**
     * Le TaskDispenser vas appeler cette methode qui va ajouter la tache dans la pile executor (qui est un thread)
     * si la file est vide alors la tache sera exécuter par rapport
     * a son type SEND/SUBSCRIBE grace au polymorphisme "task::executeTask" vas exécuter une methode de un des type
     */
    public void submit(ActionTask task) {
        executor.submit(() -> {
            try {
                task.executeTask();

                if (task.shouldForward()) {
                    taskRepeater.convertAndSend(task);
                }

                LOGGER.info("Task '{}' has been submitted{}", task.getClass().getSimpleName(), task.shouldForward() ? " and forwarded" : "");
            } catch (Exception e) {
                LOGGER.error("Error executing task: {}", task, e);
            } catch (Throwable t) {
                LOGGER.error("FATAL error in task: {}", task, t);
            }
        });
    }


}
