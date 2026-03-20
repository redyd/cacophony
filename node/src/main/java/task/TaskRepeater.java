package task;

import com.mapatiri.grammar.RegexGrammar;

import java.util.List;
import java.util.UUID;

public class TaskRepeater {
    RegexGrammar regexGrammar;

    public TaskRepeater(RegexGrammar regexGrammar) {
        this.regexGrammar = regexGrammar;
    }

    public void convertAndSend(ActionTask task){

        //build id at domain

        if (task instanceof SendTask sendTask) {
            String id = createIdDomain(sendTask.getSender());
            String mssg = regexGrammar.buildCommand("SEND", sendTask.getContent());
            String aesMssg =  mssg;
            for (String follower : sendTask.getFollowersFromOtherDomain()){
                String post = regexGrammar.buildCommand("POST", id, sendTask.getSender(),follower, aesMssg );
                //TODO AJOUTER LE FOWARD pour l'envoyer au speaker
            }


        }else if (task instanceof SubscribeTask subscribeTask) {
            //TODO ajouter encryptage aes
            String mssg = regexGrammar.buildCommand("SUBSCRIBE", subscribeTask.getToSubscribe());
            String aesMssg =  mssg;
            String id = createIdDomain(subscribeTask.getSender());
            String post = regexGrammar.buildCommand("POST", id, subscribeTask.getSender(), subscribeTask.getToSubscribe(), aesMssg);
            //TODO AJOUTER LE FOWARD pour l'envoyer au speaker

        }

    }
    private String createIdDomain(String userDomain){
        regexGrammar = new RegexGrammar(userDomain);
        List<String> components = regexGrammar.parseCommand();
       return regexGrammar.buildCommand("ID_AT_DOMAIN",  UUID.randomUUID().toString(), components.get(1) );
    }
}
