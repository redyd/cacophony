package com.mapatiri.node.service;

import com.mapatiri.node.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fait à l'aide de l'IA
 *
 * Fournit des méthodes de filtrage des abonnés locaux et distants, ainsi que des utilitaires
 * pour extraire les composantes d'une adresse réseau.
 *
 * @author Riham
 */

public class IdentityUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityUtils.class);

    /**
     * Extrait la partie login d'une adresse {@code user@domain}.
     * Si l'adresse ne contient pas de {@code @}, retourne l'entrée telle quelle.
     *
     * @param login l'adresse au format {@code user@domain} ou simplement {@code user}
     * @return la partie login sans le domaine
     */
    public String stripDomain(String login) {
        return login.contains("@") ? login.substring(0, login.indexOf('@')) : login;
    }

    /**
     * Filtre un ensemble d'abonnés selon qu'ils sont locaux ou distants par rapport à l'expéditeur.
     * L'expéditeur lui-même est toujours exclu des résultats.
     * Les abonnés locaux sont retournés sans leur domaine, les distants avec.
     *
     * @param subscribers l'ensemble des abonnés à filtrer (format {@code user@domain})
     * @param sender      l'adresse complète de l'expéditeur ({@code user@domain})
     * @param login       le login de l'expéditeur (utilisé pour l'exclure des résultats)
     * @param distant     {@code true} pour ne garder que les abonnés distants, {@code false} pour les locaux
     * @return l'ensemble filtré des abonnés
     */
    public Set<String> filterSubscribers(Set<String> subscribers, String sender, String login, boolean distant) {
        return subscribers.stream()
                .filter(f -> distant == isDistantDomain(f, sender))
                .filter(f -> !stripDomain(f).equals(login))
                .map(f -> distant ? f : stripDomain(f))
                .collect(Collectors.toSet());
    }

    /**
     * Filtre les abonnés des tags utilisés dans un message.
     * Seuls les tags présents dans {@code senderTags} sont pris en compte.
     * L'expéditeur est exclu des résultats.
     * Les abonnés locaux sont retournés sans leur domaine, les distants avec.
     *
     * @param tags       la liste complète des tags connus du système
     * @param senderTags la liste des tags utilisés dans le message par l'expéditeur
     * @param sender     l'adresse complète de l'expéditeur ({@code user@domain})
     * @param login      le login de l'expéditeur (utilisé pour l'exclure des résultats)
     * @param distant    {@code true} pour ne garder que les abonnés distants, {@code false} pour les locaux
     * @return l'ensemble des abonnés aux tags concernés, filtrés selon la localité
     */
    public Set<String> filterTagSubscribers(List<Tag> tags, List<String> senderTags, String sender, String login, boolean distant) {
        return tags.stream()
                .filter(tag -> senderTags.contains(tag.getTag()))
                .peek(tag -> LOGGER.trace("Tag {} matched, adding its {} subscribers",
                        tag.getTag(), tag.getSubscribers().size()))
                .flatMap(tag -> tag.getSubscribers().stream())
                .filter(f -> distant == isDistantDomain(f, sender))
                .filter(f -> !stripDomain(f).equals(login))
                .map(f -> distant ? f : stripDomain(f))
                .collect(Collectors.toSet());
    }

    /**
     * Extrait la partie domaine d'une adresse {@code user@domain}.
     *
     * @param userAtDomain l'adresse au format {@code user@domain}
     * @return le domaine extrait
     * @throws StringIndexOutOfBoundsException si l'adresse ne contient pas de {@code @}
     */
    public String extractDomain(String userAtDomain) {
        return userAtDomain.substring(userAtDomain.indexOf('@') + 1);
    }

    /**
     * Détermine si une cible appartient à un domaine différent de celui de l'expéditeur.
     * Accepte aussi bien des adresses complètes ({@code user@domain}) que des domaines seuls.
     *
     * @param target      la cible à analyser ({@code user@domain} ou {@code domain})
     * @param localDomain le domaine local ou l'adresse complète de l'expéditeur
     * @return {@code true} si la cible est sur un domaine différent, {@code false} sinon
     */
    public boolean isDistantDomain(String target, String localDomain) {
        String targetDomain = target.contains("@")
                ? extractDomain(target)
                : target;
        String senderDomain = localDomain.contains("@")
                ? extractDomain(localDomain)
                : localDomain;
        return !targetDomain.equals(senderDomain);
    }

    /**
     * Extrait le login depuis une adresse {@code user@domain}.
     * Lève une exception si l'adresse est nulle ou ne contient pas de {@code @}.
     *
     * @param userAtDomain l'adresse au format {@code user@domain}
     * @return le login extrait
     * @throws IllegalArgumentException si l'adresse est nulle ou mal formatée
     */
    public String getLogin(String userAtDomain) {
        if (userAtDomain == null || !userAtDomain.contains("@")) {
            throw new IllegalArgumentException("Format invalide : " + userAtDomain);
        }
        return userAtDomain.substring(0, userAtDomain.indexOf("@"));
    }

}
