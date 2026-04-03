package com.mapatiri.node.repository;

import com.mapatiri.node.exception.UserConfigException;
import com.mapatiri.node.exception.UserConflictException;
import com.mapatiri.node.model.User;
import com.mapatiri.node.model.Users;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface UserRepo {

    /**
     * Permet de charger les utilisateurs
     *
     * @param path le chemin du json qui contient les users
     * @throws UserConfigException
     */
    void loadUsers(Path path) throws UserConfigException;

    /**
     * Permet d'ajouter un utilisateur a partir de son login et de ses informations bcrypt
     *
     * @param toAdd
     */
    void addUser(User toAdd) throws IOException, UserConflictException;

    /**
     * Permet d'obtenir un user depuis son login
     *
     * @param login
     * @return
     */
    User getUserFromLogin(String login);

    /**
     * Permet d'ajouter un subscriber a un user
     *
     * @param login
     * @param subscriberId
     */
    void addSubscriberToUser(String login, String subscriberId) throws IOException;

    Set<String> getLocalSubscribersOf(String senderAtDomain);

}
