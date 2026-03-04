package com.mapatiri.node.factory;

import com.google.inject.Inject;
import com.mapatiri.node.exception.ServerSocketInitializer;
import com.mapatiri.node.model.NodeConfig;
import com.mapatiri.node.repository.ConfigRepository;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;

/**
 * Créé à l'aide de l'IA
 */
public class SocketFactory {

    private final ConfigRepository config;

    @Inject
    public SocketFactory(ConfigRepository config) {
        this.config = config;
    }

    /**
     * Permet de créer un server socket en fonction du fichier de configuration.
     *
     * @return un server socket
     * @throws ServerSocketInitializer si l'initialisation échoue
     */
    public ServerSocket createSocket() throws ServerSocketInitializer {
        NodeConfig conf = config.getConfig();
        return conf.isTls()
                ? secureSocket(conf)
                : simpleSocket(conf);
    }

    private ServerSocket simpleSocket(NodeConfig conf) throws ServerSocketInitializer {
        try {
            return new ServerSocket(conf.getUnicastPort());
        } catch (IOException e) {
            throw new ServerSocketInitializer(e);
        }
    }

    private ServerSocket secureSocket(NodeConfig conf) throws ServerSocketInitializer {
        try {
            SSLContext ctx = buildContext(conf);

            SSLServerSocket socket =
                    (SSLServerSocket) ctx.getServerSocketFactory()
                            .createServerSocket(conf.getUnicastPort());

            socket.setEnabledProtocols(new String[]{"TLSv1.3"});
            return socket;
        } catch (Exception e) {
            throw new ServerSocketInitializer(e);
        }
    }

    private SSLContext buildContext(NodeConfig conf) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(NodeConfig.KEYSTORE_PATH.toFile())) {
            ks.load(fis, conf.getKeystorePassword().toCharArray());
        }

        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        kmf.init(ks, conf.getKeystorePassword().toCharArray());

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, new SecureRandom());

        return ctx;
    }
}