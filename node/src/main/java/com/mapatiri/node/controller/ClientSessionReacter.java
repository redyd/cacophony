package com.mapatiri.node.controller;

import com.mapatiri.node.model.ClientConnection;

/**
 * À implémenter dans les classes où l'on veut être mis à jour des connexions et déconnexion des utilisateurs
 */
public interface ClientSessionReacter {

    /**
     * Méthode apple lorsque
     * @param user
     */
    void onUserLogout(ClientConnection user);

    void onUserLogin(ClientConnection user);
}
