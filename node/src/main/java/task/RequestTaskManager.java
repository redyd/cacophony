package task;

import com.mapatiri.node.exception.BadRequestException;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestTaskManager {

    private TaskRepeater taskRepeater ;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    public RequestTaskManager(TaskRepeater taskRepeater) {
        this.taskRepeater = taskRepeater;
    }


    /**
     * Le TaskDispenser vas appeler cette methode qui vas ajouter la tache dans la pile executor (qui est un thread)
     * si la file est vide alors la tache sera executer par rapport
     * a son type SEND/SUBSCRIBE grace au polymorfisme "task::executeTask" vas executer une methode de un des type
     * @param task
     */
    public void submit (ActionTask task ) {
        executor.submit(task::executeTask);
       if(task.shouldForward()){
           taskRepeater.convertAndSend(task);
       }

    }

//    private ActionTask process() throws BadRequestException {
//
//        executor.submit(()->
//                while(!executor.isShutdown()){
//                    ActionTask task =
//        }
//
}
