package com.mapatiri.node.task;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestTaskManager {

    private TaskRepeater taskRepeater;
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestTaskManager.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public RequestTaskManager(TaskRepeater taskRepeater) {
        this.taskRepeater = taskRepeater;
    }


    /**
     * Le TaskDispenser vas appeler cette methode qui vas ajouter la tache dans la pile executor (qui est un thread)
     * si la file est vide alors la tache sera executer par rapport
     * a son type SEND/SUBSCRIBE grace au polymorfisme "task::executeTask" vas executer une methode de un des type
     *
     * @param task
     */
    public void submit(ActionTask task) {
        LOGGER.debug("Submitting task: {}", task);
        executor.submit(() -> {
            try {
                LOGGER.debug(">>> BEFORE executeTask: {}", task);
                task.executeTask();
                LOGGER.debug(">>> AFTER executeTask: {}", task);

                if (task.shouldForward()) {
                    LOGGER.debug(">>> BEFORE forward: {}", task);
                    taskRepeater.convertAndSend(task);
                    LOGGER.debug(">>> AFTER forward: {}", task);
                }
            } catch (Exception e) {
                LOGGER.error("Error executing task: {}", task, e);
            } catch (Throwable t) {
                LOGGER.error("FATAL error in task: {}", task, t);
            }
        });
    }


}
