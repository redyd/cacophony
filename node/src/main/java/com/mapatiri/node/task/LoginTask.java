package com.mapatiri.node.task;

import com.google.inject.Inject;
import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.model.ClientConnection;
import com.mapatiri.node.model.User;
import com.mapatiri.node.repository.UserRepo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Permet d'authentifier un utilisateur déjà enregistré.
 */
public class LoginTask implements AuthTask {

    private final UserRepo userRepository;

    @Inject
    public LoginTask(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ClientConnection authenticate(Socket client, BufferedReader in, PrintWriter out, RegexGrammar response, String randomString) throws AuthentificationFailed {
        try {
            User target = tryGettingUser(out, response);

            if (target == null) {
                out.println("Invalid credentials (user not found)");
                throw new AuthentificationFailed("Invalid credentials (user not found)");
            }

            String hashResponse = tryGettingHashResponse(in, out, target);
            String expectedHash = hash(randomString + target.getHashedPassword());

            if (!expectedHash.equals(hashResponse)) {
                String invalidCredentials = new RegexGrammar().buildCommand("ERROR", "Invalid credentials");
                out.println(invalidCredentials);
                throw new AuthentificationFailed("Invalid credentials");
            }

            ClientConnection connection = new ClientConnection(client, target, in, out);

            String ok = new RegexGrammar()
                    .buildCommand("OK", "Welcome back to Cacophony %s".formatted(connection.getUser().getLogin()));
            connection.send(ok);

            return connection;
        } catch (IOException | NoSuchAlgorithmException e) {
            String unexpectedError = new RegexGrammar().buildCommand("ERROR", "Unexpected error");
            out.println(unexpectedError);
            throw new AuthentificationFailed("Something went wrong");
        }
    }

    /**
     * Effectue le challenge avec le client, et récupère le hash du client.
     * En cas d'erreur, lance un {@link AuthentificationFailed}.
     */
    private String tryGettingHashResponse(BufferedReader in, PrintWriter out, User target) throws IOException, AuthentificationFailed {
        // send challenge
        String challenge = new RegexGrammar()
                .buildCommand("CHALLENGE", String.valueOf(target.getBcryptRound()), target.getBcryptSalt());
        out.println(challenge);

        // wait for challenge response
        RegexGrammar challengeResponse = new RegexGrammar(in.readLine());
        List<String> challengeResponseValues = challengeResponse.parseCommand();

        // check challenge response validity
        if (challengeResponseValues.size() != 1
                || !"RESPONSE".equals(challengeResponse.getTypeOfCommand())
                || challengeResponseValues.getFirst().isBlank()) {
            String responseError = new RegexGrammar().buildCommand("ERROR", "Invalid response from challenge");
            out.println(responseError);
            throw new AuthentificationFailed("Invalid response from challenge");
        }

        return challengeResponseValues.getFirst();
    }

    /**
     * Tente de récupérer depuis le JSON l'utilisateur mentionné dans la réponse Regex
     */
    private User tryGettingUser(PrintWriter out, RegexGrammar response) throws AuthentificationFailed {
        List<String> userLoginResponse = response.parseCommand();

        // check welcome validity
        if (userLoginResponse.size() != 1 || userLoginResponse.getFirst().isBlank()) {
            String responseError = new RegexGrammar().buildCommand("ERROR", "Invalid response to welcome");
            out.println(responseError);
            throw new AuthentificationFailed("Invalid response to welcome");
        }

        String login = userLoginResponse.getFirst();
        return this.userRepository.getUserFromLogin(login);
    }

    private String hash(String toHash) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        byte[] val = digest.digest(toHash.getBytes());
        return bytesToHex(val);
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
