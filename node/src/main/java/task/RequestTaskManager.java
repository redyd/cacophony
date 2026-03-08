package task;

import com.mapatiri.node.exception.BadRequestException;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestTaskManager {


    ExecutorService executor = Executors.newSingleThreadExecutor();


    /**
     * Le TaskDispenser vas appeler cette methode qui vas ajouter la tache dans la pile executor (qui est un thread)
     * si la file est vide alors la tache sera executer par rapport
     * a son type SEND/SUBSCRIBE grace au polymorfisme "task::executeTask" vas executer une methode de un des type
     * @param task
     */
    public void submit (ActionTask task ) {
        executor.submit(task::executeTask);
    }

//    private ActionTask process() throws BadRequestException {
//
//        executor.submit(()->
//                while(!executor.isShutdown()){
//                    ActionTask task =
//        }
//
//
//
//                    )
//
//
////1. Trim espaces avant/après + vérifier \n final
////2. Extraire premier mot (SEND/POST/SUBSCRIBE)
////3. Choisir regex ABNF selon type
////4. Appliquer regex sur chaîne complète
////5. Vérifier match 100% (pas de reste)
////6. Extraire groupes nommés (message/id/sender/etc)
////7. Valider longueurs/formats ABNF
////8. Créer ActionTask appropriée
////9. Remplir ActionTask avec données
////10. this.submit(actionTask)
////11. Si erreur n'importe où → BadRequestException()
//
//        return null;
//    }
}
