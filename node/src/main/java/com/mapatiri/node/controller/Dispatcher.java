package com.mapatiri.node.controller;

import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.BadRequestException;
import com.mapatiri.node.model.ClientConnection;

import java.net.Socket;

/**
 * Redispatch les interactions avec le client, entre la connexion et la soumission d'un message.
 *
 * @author Timëo
 */
public interface Dispatcher {

    /**
     * Démarre un ping pong d'authentification avec la connexion.
     *
     * @param socket la connexion client
     * @return la connexion client enregistrée dans le {@link com.mapatiri.node.service.ConnectionStore}
     * @throws AuthentificationFailed si l'authentification échoue
     */
    ClientConnection startAuth(Socket socket) throws AuthentificationFailed;

    /**
     * Soumet un message au node.
     *
     * @param request la requête
     * @param sender le login de l'utilisateur
     * @throws BadRequestException si la requête est mal formée
     */
    void submit(String request, String sender) throws BadRequestException;
}
