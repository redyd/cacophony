package com.mapatiri.node.service;

import com.mapatiri.node.model.BcryptParams;

public class AuthService implements BcryptService {

    @Override
    public BcryptParams parseByCrypt(String byCryptString) {//ai
        // Vérifie format Bcrypt "$2b$14$..."
        if (byCryptString == null || !byCryptString.startsWith("$2b$")) {
            throw new IllegalArgumentException("Format Bcrypt invalide: " + byCryptString);
        }

        // Split sur "$" → ["", "2b", "14", "salt+bcryptHash"]
        String[] parts = byCryptString.split("\\$");

        if (parts.length < 4) {
            throw new IllegalArgumentException("Bcrypt malformé: " + byCryptString);
        }
        // parts[2] = counter (ex: "14"), parts[3] = salt+bcryptHash (22 chars salt)
        int counter = Integer.parseInt(parts[2]);
        String salt = parts[3].substring(0, 22);  // Premier 22 chars = salt

        return new BcryptParams(counter, salt);
    }
}
