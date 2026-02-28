package com.mapatiri.node;

import config.JsonServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Dans votre main() ou test
        try {
            JsonServiceImpl service = new JsonServiceImpl("node-config.json");

            // ✅ Utilisez getUser() au lieu de nodeConfig direct
            System.out.println("Domain: " + service.loadNodeConfig().getDomain());

            service.addUser("louis", 22, "$2b$14$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLmnopqrst");

            // ✅ Vérifiez via API publique
            service.getUser("louis").ifPresent(user ->
                    System.out.println("User louis créé: " + user.getSaltLenght())
            );

            System.out.println("Abonnements louis: " + service.getSubscribers("louis"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        }



}
