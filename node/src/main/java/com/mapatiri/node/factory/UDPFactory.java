package com.mapatiri.node.factory;

import com.google.inject.Inject;
import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * Factory pour le socket UDP.
 *
 * @author Mattéo
 */
public class UDPFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(UDPFactory.class);

    private final ConfigRepository configRepository;

    @Inject
    public UDPFactory(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public MulticastSocket create() throws SocketCreation {
        try {
            var config = configRepository.getConfig();

            NetworkInterface networkInterface = NetworkInterface.getByName(config.getNetworkInterface());

            MulticastSocket socket = new MulticastSocket(config.getMulticastPort());

            if (networkInterface != null) {
                socket.setNetworkInterface(networkInterface);
            } else {
                LOGGER.warn("Network interface '{}' not found. Please edit your config.", config.getNetworkInterface());
                throw new SocketCreation("Could not create multicast socket");
            }

            return socket;
        } catch (Exception e) {
            LOGGER.error("Could not create MulticastSocket: {}", e.getMessage());
            throw new SocketCreation("Could not create MulticastSocket: " + e.getMessage());
        }
    }
}
