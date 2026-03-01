package com.mapatiri.node.factory;


import com.google.inject.Inject;
import com.mapatiri.node.exception.TLSException;
import com.mapatiri.node.service.ConfigService;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class TLSContextFactory {

    private final ConfigService config;

    @Inject
    public TLSContextFactory(ConfigService config) {
        this.config = config;
    }

    public SSLContext createContext() throws TLSException {
        try {
            String path = "keystore.p12"; // TODO récupérer depuis la config
            char[] password = "PASSWORD".toCharArray(); // TODO récupérer depuis la config

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(path), password);

            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            kmf.init(ks, password);

            SSLContext ctx = SSLContext.getInstance("TLSv1.3");
            ctx.init(kmf.getKeyManagers(), null, null);

            return ctx;
        } catch (UnrecoverableKeyException | CertificateException | KeyStoreException | IOException |
                 NoSuchAlgorithmException | KeyManagementException e) {
            throw new TLSException(e);
        }
    }
}