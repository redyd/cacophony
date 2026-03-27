package com.mapatiri.node.controller;

import com.mapatiri.node.model.ClientConnection;

/**
 * À implémenter dans les classes où l'on veut être mis à jour des connexions et déconnexion des utilisateurs.
 */
public interface ClientSessionReacter {

    /**
     * Méthode appelée lorsqu'un utilisateur se déconnecte.
     *
     * @param user l'utilisateur déconnecté
     */
    void onUserLogout(ClientConnection user);

    /**
     * Méthode appelée lorsqu'un utilisateur se connecte.
     *
     * @param user l'utilisateur connecté
     */
    void onUserLogin(ClientConnection user);
}
