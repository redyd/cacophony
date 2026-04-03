package com.mapatiri.speaker.service;

import com.mapatiri.speaker.model.NodeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeStore {

    private static final Logger logger = LoggerFactory.getLogger(NodeStore.class);
    private final Map<String, NodeConnection> connections = new ConcurrentHashMap<>();

    public void Add(NodeConnection nodeConnection) {
        connections.put(nodeConnection.getDomain(), nodeConnection);
    }

    public void remove(String domain) {
        connections.remove(domain);
    }

    public boolean isConnected(String domain) {
        return connections.containsKey(domain);
    }

    public void broadcast(String destDomain, String message) {
        NodeConnection nodeConnection = connections.get(destDomain);
        if (nodeConnection == null) {
            logger.info("Node destinataire non connecte : {}", destDomain);
            return;
        }
        try {
            logger.debug("Message envoyé vers node2 : '{}'", message);
            nodeConnection.send(message);
            logger.info("Message forward vers {}", destDomain);
        } catch (IOException e) {
            logger.error("Erreur lors du forward vers {} : {}", destDomain, e.getMessage());
        }
    }
}
