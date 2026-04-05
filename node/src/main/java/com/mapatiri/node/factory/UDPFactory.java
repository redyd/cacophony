package com.mapatiri.node.factory;

import com.google.inject.Inject;
import com.mapatiri.node.exception.SocketCreation;
import com.mapatiri.node.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
                LOGGER.info("Here are the available network interface: {}", getAvailableInterfaces());
            }

            return socket;
        } catch (Exception e) {
            LOGGER.error("Could not create MulticastSocket: {}", e.getMessage());
            throw new SocketCreation("Could not create MulticastSocket: " + e.getMessage());
        }
    }

    public String getAvailableInterfaces() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        List<String> results = new ArrayList<>();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();

            // Filtres principaux
            if (!ni.isUp()) continue;
            if (ni.isLoopback()) continue;
            if (!ni.getInetAddresses().hasMoreElements()) continue;

            String name = ni.getName();
            String display = ni.getDisplayName();

            // Exclusion des interfaces "polluantes"
            if (display.contains("WAN Miniport")) continue;
            if (display.contains("Npcap")) continue;
            if (display.contains("QoS")) continue;
            if (display.contains("Filter")) continue;

            if (ni.isVirtual()) continue;

            results.add(name);
        }

        return results.toString();
    }
}
