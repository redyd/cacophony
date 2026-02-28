package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeConfig {
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
    private Map<String, User> users = new HashMap<>();
    //les abonnement de chaque utilisateur du node
    private Map<String, Set<String>> subscriptions = new HashMap<>();

    // Constructeur vide (pour Gson)
    //pour fonctionner gson a besoin dun constructeur vide
    public NodeConfig() {
    }

    // Constructeur complet
    public NodeConfig(String domain, int tcpClientPort, int udpMulticastPort,
                      String multicastAddress, String aesSpeakerKey,
                      String tlsKeystore, String tlsKeystorePassword) {
        this.domain = domain;
        this.tcpClientPort = tcpClientPort;
        this.udpMulticastPort = udpMulticastPort;
        this.multicastAddress = multicastAddress;
        this.aesSpeakerKey = aesSpeakerKey;
        this.tlsKeystore = tlsKeystore;
        this.tlsKeystorePassword = tlsKeystorePassword;
        this.users = new HashMap<>();
        this.subscriptions = new HashMap<>();
    }

    // Getters et Setters
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getTcpClientPort() {
        return tcpClientPort;
    }

    public void setTcpClientPort(int tcpClientPort) {
        this.tcpClientPort = tcpClientPort;
    }

    public int getUdpMulticastPort() {
        return udpMulticastPort;
    }

    public void setUdpMulticastPort(int udpMulticastPort) {
        this.udpMulticastPort = udpMulticastPort;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public void setMulticastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    public String getAesSpeakerKey() {
        return aesSpeakerKey;
    }

    public void setAesSpeakerKey(String aesSpeakerKey) {
        this.aesSpeakerKey = aesSpeakerKey;
    }

    public String getTlsKeystore() {
        return tlsKeystore;
    }

    public void setTlsKeystore(String tlsKeystore) {
        this.tlsKeystore = tlsKeystore;
    }

    public String getTlsKeystorePassword() {
        return tlsKeystorePassword;
    }

    public void setTlsKeystorePassword(String tlsKeystorePassword) {
        this.tlsKeystorePassword = tlsKeystorePassword;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<String, Set<String>> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Map<String, Set<String>> subscriptions) {
        this.subscriptions = subscriptions;
    }
}