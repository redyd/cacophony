package com.mapatiri.node.repository;

import com.mapatiri.node.exception.UserConflictException;
import com.mapatiri.node.model.User;

import java.io.IOException;
import java.util.Set;

public interface UserRepo {


    /**
     * Permet d'ajouter un utilisateur a partir de son login et de ses informations bcrypt
     */
    void addUser(User toAdd) throws IOException, UserConflictException;

    /**
     * Permet d'obtenir un user depuis son login
     */
    User getUserFromLogin(String login);

    /**
     * Permet d'ajouter un subscriber a un user
     */
    void addSubscriberToUser(String login, String subscriberId) throws IOException;

    Set<String> getLocalSubscribersOf(String senderAtDomain);

}
