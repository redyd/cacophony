package com.mapatiri.node.config;

import com.mapatiri.node.model.BcryptParams;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.model.User;

import java.util.List;
import java.util.Optional;

public interface JsonService {

    NodeConfig loadNodeConfig() throws NodeFileException;

    void addUser(String username,int saltLenght, String bycrypt) throws NodeConfigSaveException;

    Optional<User> getUser(String username);

    void addSubscription(String username,String subscription) throws NodeConfigSaveException;

    /**
     * Retourne les abonné au tag/user
     * @param subscription
     * @return
     */
    List<String> getSubscribers(String subscription);


    /**
     * Enregistrer les info dans le config file
     * @param nodeConfig
     *
     */
    void saveConfig(NodeConfig nodeConfig) throws NodeConfigSaveException;

    /**
     * Permet d'extraire les param lors du challenge pour un user apres avoir utilisé getUser
     * @param byCryptString
     * @return
     */
    BcryptParams parseByCrypt ( String byCryptString );

    

}
