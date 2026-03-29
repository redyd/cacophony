package com.mapatiri.node.model;

/**
 * Paramètre bcrypt d'un mot de passe.
 *
 * @param round
 * @param hash
 * @param salt
 *
 * @author Timëo
 */
public record BcryptParams(int round, String hash, String salt) {
}
