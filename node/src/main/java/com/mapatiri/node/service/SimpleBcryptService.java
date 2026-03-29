package com.mapatiri.node.service;

import com.mapatiri.node.model.BcryptParams;

import java.util.Arrays;

/**
 * Classe de parsing bcrypt.
 *
 * @author Riham
 */
public class SimpleBcryptService implements BcryptService {

    @Override
    public BcryptParams parseByCrypt(String bcrypt, int saltLength) {//ai
        // Vérifie format Bcrypt "$2b$14$..."
        if (bcrypt == null || !bcrypt.startsWith("$2b$")) {
            throw new IllegalArgumentException("Format Bcrypt invalide: " + bcrypt);
        }

        // Split sur "$" → ["", "2b", "14", "salt+bcryptHash"]
        String[] parts = Arrays.stream(bcrypt.split("\\$"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        if (parts.length != 3) {
            throw new IllegalArgumentException("Bcrypt malformé: " + bcrypt);
        }

        // part 0: 2b, part 1: round count, part 2: salt+hash
        int counter = Integer.parseInt(parts[1]);
        String salt = parts[2].substring(0, saltLength);
        String hash = parts[2].substring(saltLength);

        return new BcryptParams(counter, hash, salt);
    }
}
