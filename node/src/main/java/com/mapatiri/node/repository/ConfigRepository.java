package com.mapatiri.node.repository;

import com.mapatiri.node.model.NodeConfig;

/**
 * Repository de la config.
 *
 * @author Timëo
 */
public interface ConfigRepository {

    /**
     * Récupère la config du Node.
     *
     * @return la config du Node
     */
    NodeConfig getConfig();

    /**
     * Construit un élément à l'adresse actuelle : 'something@domain.xyz'.
     */
    String buildAtDomain(String element);
}
