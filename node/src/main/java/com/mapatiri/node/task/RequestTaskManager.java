package com.mapatiri.node.task;

import com.google.inject.Inject;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestTaskManager {

    private TaskRepeater taskRepeater;

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
        //les deux en meme temps vu que cest le mem thread
        executor.submit(() -> {
            task.executeTask();
            if (task.shouldForward()) {
                taskRepeater.convertAndSend(task);
            }
        });
    }


}
