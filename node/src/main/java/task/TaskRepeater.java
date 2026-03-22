package task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.service.ConnectionStore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskRepeater {
   private RegexGrammar regexGrammar;



    public void convertAndSend(ActionTask task){

        //build id at domain

        if (task instanceof SendTask sendTask) {
            String id = regexGrammar.generateIdAtDomain(sendTask.getSender());

            String mssg = regexGrammar.buildCommand("MSG", sendTask.getSender(), sendTask.getContent());
            String aesMssg =  mssg;
            for (String follower : sendTask.getFollowersFromOtherDomain()){

                Optional<ClientConnection> OptionalCon = sendTask.getFollowerCon(follower);
                ClientConnection con = null;
                if(OptionalCon.isPresent()){
                     con = OptionalCon.get();
                    if (con.hasAlreadyReceived(id)) {
                        continue;
                    }
                    String post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), follower, aesMssg);
                }
                    //TODO AJOUTER LE FOWARD pour l'envoyer au speaker
            }


        }else if (task instanceof SubscribeTask subscribeTask) {
            //TODO ajouter encryptage aes
            String mssg = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String aesMssg =  mssg;
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
            String post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), aesMssg);
            //TODO AJOUTER LE FOWARD pour l'envoyer au speaker
        }

    }
    //todo a mettre dans le la grammaire


}
