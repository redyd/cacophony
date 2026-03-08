package task;

import com.mapatiri.node.exception.BadRequestException;

public class TaskDispatcher {
    //Grammar
    private RequestTaskManager requestTaskManager;


    public void submit(String request, String senderDomain) throws BadRequestException{
        //1. Trim espaces avant/après + vérifier \n final
        //2. Extraire premier mot (SEND/POST/SUBSCRIBE)
        //3. Choisir regex ABNF selon type
        //4. Appliquer regex sur chaîne complète
        //5. Vérifier match 100% (pas de reste)
        //6. Extraire groupes nommés (message/id/sender/etc)
        //7. Valider longueurs/formats ABNF
        //8. Créer ActionTask appropriée par rapport a send ou subscribe
        //9. Remplir ActionTask avec données
        //10. this.submit(actionTask)
        //11. Si erreur n'importe où → BadRequestException()



    }
}
