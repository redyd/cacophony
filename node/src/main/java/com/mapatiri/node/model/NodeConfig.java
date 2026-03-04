package com.mapatiri.node.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
public class NodeConfig {
    // Getters et Setters
    //le nom du domaine du node
    private String domain;
    //port tcp client phython
    private int tcpClientPort;
    private int udpMulticastPort;
    //adresse ip multicast que utilise les nodes pour etre decouvert par le speaker
    private String multicastAddress;
    //cle AES-256 partage entre le speaker et un node pour securise les post des nodes
    private String aesSpeakerKey;
    //certificat serveur tls , Fichier keystore PKCS12 pour TLS 1.3
    private String tlsKeystore;
    //Mot de passe du keystore TLS (ex: "changeit"). Nécessaire pour charger le certificat.
    private String tlsKeystorePassword;
    //utilisateur du node le string est le nom de lutilisaeur
    private Set<User> users;
    //les abonnement de chaque utilisateur du node
    private Set<Tag> subscriptions;

    // Constructeur vide (pour Gson)
    //pour fonctionner gson a besoin dun constructeur vide
    public NodeConfig() {
    }
}