package com.mapatiri.node.factory;

import com.mapatiri.node.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class UDPFactory {
    private static final Logger logger = LoggerFactory.getLogger(UDPFactory.class);

    public MulticastSocket create(ConfigRepository configRepository) {
        try {
            var config = configRepository.getConfig();

            NetworkInterface networkInterface = NetworkInterface.getByName(config.getNetworkInterface());
            logger.debug("network interface {}", networkInterface);

            MulticastSocket socket = new MulticastSocket(config.getMulticastPort());
            logger.debug("socket {}", socket);

            if (networkInterface != null) {
                logger.debug("network interface {}", networkInterface);
                socket.setNetworkInterface(networkInterface);
            } else {
                logger.warn("Interface reseau '{}' non trouvee", config.getNetworkInterface());
            }

            logger.info("MulticastSocket cree");
            return socket;

        } catch (Exception e) {
            logger.error("Echec de la creation du MulticastSocket: {}", e.getMessage());
            throw new RuntimeException("UDPFactory a echoue", e);
        }
    }
}
