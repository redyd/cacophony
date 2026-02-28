package config;

import model.BcryptParams;
import model.NodeConfig;
import model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class JsonServiceImpl implements JsonService {
    private NodeConfig nodeConfig;
    private final Path path;

    public JsonServiceImpl(String filename) throws NodeFileException {
        path = getPath(filename);
        nodeConfig = loadNodeConfig();

    }

    @Override
    public NodeConfig loadNodeConfig() throws NodeFileException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        NodeConfig config = new NodeConfig();
        try {

            config = gson.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8),
                    NodeConfig.class);
        } catch (IOException e) {
            System.out.println("Erreur loading nodeConfig" +  e.getMessage());
            throw new NodeFileException(e.getMessage());
        }

        if (config == null) {
            throw new NodeFileException("Config vide pour: " + path);
        }


        return config;
    }

    @Override
    public void addUser(String username, int saltLenght, String bycrypt) throws NodeConfigSaveException {


        if (nodeConfig == null) {
            throw new IllegalStateException("NodeConfig pas chargé");
        }

        synchronized (this) {
            Map<String, User> users = this.nodeConfig.getUsers();
            users.put(username, new User(saltLenght, bycrypt));
            saveConfig(nodeConfig);
        }


    }

    @Override
    public Optional<User> getUser(String username) {
        if (nodeConfig == null) {
            return Optional.empty();
        }
        Map<String, User> users = this.nodeConfig.getUsers();
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public void addSubscription(String username, String subscription) throws NodeConfigSaveException {
        if (nodeConfig == null) {
            throw new IllegalStateException("NodeConfig pas chargé");
        }
        synchronized (this) {
            nodeConfig.getSubscriptions()//ai
                    .computeIfAbsent(username, k -> new HashSet<>())
                    .add(subscription);
            saveConfig(nodeConfig);

        }


    }

    @Override
    public List<String> getSubscribers(String username) {
        if (nodeConfig == null) return List.of(); //ai
        Set<String> subs = nodeConfig.getSubscriptions().get(username);
        return subs != null ? List.copyOf(subs) : List.of();//ai


    }

    @Override
    public void saveConfig(NodeConfig nodeConfig) throws NodeConfigSaveException {
        try {
            //synchronized cest comme transaction car il se peut quon utilise les methode en meme temps
            synchronized (this) {
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create();
                Path tempFile = Files.createTempFile("nodeConfig", ".json");

                try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                    gson.toJson(nodeConfig, writer);
                }
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);

            }

        } catch (IOException e) {
            throw new NodeConfigSaveException(e.getMessage());

        }

    }

    @Override
    public BcryptParams parseByCrypt(String byCryptString) {//ai
        // Vérifie format Bcrypt "$2b$14$..."
        if (byCryptString == null || !byCryptString.startsWith("$2b$")) {
            throw new IllegalArgumentException("Format Bcrypt invalide: " + byCryptString);
        }

        // Split sur "$" → ["", "2b", "14", "salt+hash"]
        String[] parts = byCryptString.split("\\$");

        if (parts.length < 4) {
            throw new IllegalArgumentException("Bcrypt malformé: " + byCryptString);
        }
        // parts[2] = counter (ex: "14"), parts[3] = salt+hash (22 chars salt)
        int counter = Integer.parseInt(parts[2]);
        String salt = parts[3].substring(0, 22);  // Premier 22 chars = salt

        return new BcryptParams(counter, salt);
    }

    private Path getPath(String filename) {
        return Path.of("src", "main", "resources", filename);
    }
}
