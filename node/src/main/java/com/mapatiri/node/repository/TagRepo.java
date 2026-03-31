package com.mapatiri.node.repository;

import com.mapatiri.node.model.Tag;
import com.mapatiri.node.model.Tags;

import java.io.IOException;

public interface TagRepo {

    /**
     * Ajoute un tag.
     *
     * @param tag tag à ajouter
     */
    void addTag(Tag tag) throws IOException;

    /**
     * Abonne un utilisateur à un tag.
     *
     * @param tagName le nom du tag
     * @param subscriber le login@domain de l'utilisateur
     */
    void addSubscriberToTag(String tagName, String subscriber) throws IOException;

    /**
     * Récupère tous les tags
     */
    Tags getAllTags();

    /**
     * Vérifie si un tag existe pour ce node.
     */
    boolean tagExist(Tag tag);
//    boolean tagExistInDomain(String tagName, String domain);
}
