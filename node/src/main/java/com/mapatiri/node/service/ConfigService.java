package com.mapatiri.node.service;

import com.mapatiri.node.exception.NodeConfigSaveException;
import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.model.User;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ConfigService {

    NodeConfig loadConfig(Path path) throws NodeFileException;

    User addUser(String username, String bcrypt, int saltLength) throws NodeConfigSaveException;

    Optional<User> getUser(String username);

    void addSubscription(User to, String subscriber) throws NodeConfigSaveException;

    /**
     * Retourne les abonné au tag/user
     *
     * @param subscription
     * @return
     */
    List<String> getSubscribers(String subscription);

    /**
     * Enregistrer les info dans le config file
     *
     * @param nodeConfig
     *
     */
    void saveConfig(NodeConfig nodeConfig) throws NodeConfigSaveException;
}
