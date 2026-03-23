package task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.service.ConnectionStore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskRepeater {
    private RegexGrammar regexGrammar = new RegexGrammar();

    @Inject
    public TaskRepeater() {}

    public void convertAndSend(ActionTask task) {


        if (task instanceof SendTask sendTask) {
            String id = regexGrammar.generateIdAtDomain(sendTask.getSender());

            String mssg = regexGrammar.buildCommand("MSG", sendTask.getSender(), sendTask.getContent());
            String aesMssg = mssg;
            for (String follower : sendTask.getFollowersFromOtherDomain()) {

                String post = regexGrammar.buildCommand("POST", id, sendTask.getSender(), follower, aesMssg);

                //TODO AJOUTER LE FOWARD pour l'envoyer au speaker
            }

        } else if (task instanceof SubscribeTask subscribeTask) {
            //TODO ajouter encryptage aes
            String mssg = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String aesMssg = mssg;
            String id = regexGrammar.generateIdAtDomain(subscribeTask.getSender());
            String post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), aesMssg);
            //TODO AJOUTER LE FOWARD pour l'envoyer au speaker
        }

    }
    //todo a mettre dans le la grammaire


}
