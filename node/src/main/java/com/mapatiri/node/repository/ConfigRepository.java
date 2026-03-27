package com.mapatiri.node.repository;

import com.mapatiri.node.exception.NodeFileException;
import com.mapatiri.node.model.NodeConfig;

import java.nio.file.Path;

public interface ConfigRepository {

    /**
     * Charge le fichier de configuration.
     *
     * @param path le chemin relatif de la configuration
     * @throws NodeFileException si le fichier est invalide ou non trouvé
     */
    void loadConfig(Path path) throws NodeFileException;

    /**
     * Récupère la config du Node
     * @return la config du Node
     */
    NodeConfig getConfig();

    String buildLoginAtCurrentDomain(String login);
}
