package config;

import model.BcryptParams;
import model.NodeConfig;
import model.User;

import java.util.List;

public interface JsonService {

    NodeConfig loadNodeConfig(String filename);

    void addUser(String username,int saltLenght, String bycrypt);

    User getUser(String username);

    void addSubscription(String username,String subscription);

    /**
     * Retourne les abonné au tag/user
     * @param subscription
     * @return
     */
    List<String> getSubscribers(String subscription);


    /**
     * Enregistrer les info dans le config file
     * @param nodeConfig
     * @param filename
     */
    void saveConfig(NodeConfig nodeConfig, String filename);

    /**
     * Permet d'extraire les param lors du challenge pour un user apres avoir utilisé getUser
     * @param byCryptString
     * @return
     */
    BcryptParams parseByCrypt ( String byCryptString );

    

}
