package com.mapatiri.node;

import com.mapatiri.node.exception.NodeFileException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileConfiguration {

    private static final Path RESOURCES_FOLDER = Path.of("src", "main", "resources");
    private static final Path DATA_FOLDER = RESOURCES_FOLDER.resolve("data");

    private static final Path DEFAULT_CONFIG_FILE = RESOURCES_FOLDER.resolve("node-config.json");
    private static final Path DEFAULT_USER_FILE = DATA_FOLDER.resolve("user.json");
    private static final Path DEFAULT_TAG_FILE = DATA_FOLDER.resolve("tag.json");

    public Path resolveUserPath(String[] args) throws NodeFileException {
        ParsedArguments parsedArguments = parseArguments(args);
        if (parsedArguments.useDefaults()) {
            return DEFAULT_USER_FILE;
        }
        return resolveExistingDataPath(parsedArguments.users());
    }

    public Path resolveTagPath(String[] args) throws NodeFileException {
        ParsedArguments parsedArguments = parseArguments(args);
        if (parsedArguments.useDefaults()) {
            return DEFAULT_TAG_FILE;
        }
        return resolveExistingDataPath(parsedArguments.tags());
    }

    public Path resolveConfigPath(String[] args) throws NodeFileException {
        ParsedArguments parsedArguments = parseArguments(args);
        if (parsedArguments.useDefaults()) {
            return DEFAULT_CONFIG_FILE;
        }

        String target = parsedArguments.config();
        Path path = Path.of(target);

        if (Files.exists(path)) {
            return path;
        }

        Path jsonPath = RESOURCES_FOLDER.resolve(target);
        if (Files.exists(jsonPath)) {
            return jsonPath;
        }

        Path shortNamePath = RESOURCES_FOLDER.resolve(target + ".json");
        if (Files.exists(shortNamePath)) {
            return shortNamePath;
        }

        throw new NodeFileException("Invalid argument for the configuration");
    }

    private ParsedArguments parseArguments(String[] args) throws NodeFileException {
        if (hasNoArguments(args)) {
            return ParsedArguments.defaults();
        }

        boolean namedArguments = false;
        boolean positionalArguments = false;
        for (String arg : args) {
            if (arg != null && arg.contains("=")) {
                namedArguments = true;
            } else {
                positionalArguments = true;
            }
        }

        if (namedArguments && positionalArguments) {
            throw new NodeFileException("Do not mix positional arguments with named arguments");
        }

        if (namedArguments) {
            return parseNamedArguments(args);
        }

        return parsePositionalArguments(args);
    }

    private ParsedArguments parsePositionalArguments(String[] args) throws NodeFileException {
        if (args.length < 3 || isBlank(args[0]) || isBlank(args[1]) || isBlank(args[2])) {
            throw new NodeFileException("Either provide no arguments or provide config, users and tags together");
        }

        return new ParsedArguments(args[0], args[1], args[2], false);
    }

    private ParsedArguments parseNamedArguments(String[] args) throws NodeFileException {
        Map<String, String> values = new HashMap<>();
        for (String arg : args) {
            int separatorIndex = arg.indexOf('=');
            if (separatorIndex <= 0 || separatorIndex == arg.length() - 1) {
                throw new NodeFileException("Invalid named argument: " + arg);
            }

            String key = arg.substring(0, separatorIndex).trim();
            String value = arg.substring(separatorIndex + 1).trim();
            if (!isSupportedKey(key)) {
                throw new NodeFileException("Unknown argument: " + key);
            }
            if (isBlank(value)) {
                throw new NodeFileException("Missing value for argument: " + key);
            }
            if (values.put(key, value) != null) {
                throw new NodeFileException("Duplicate argument: " + key);
            }
        }

        if (!values.keySet().containsAll(Set.of("config", "users", "tags"))) {
            throw new NodeFileException("Named arguments must provide config, users and tags");
        }

        return new ParsedArguments(values.get("config"), values.get("users"), values.get("tags"), false);
    }

    private Path resolveExistingDataPath(String target) throws NodeFileException {
        Path directPath = Path.of(target);
        if (Files.exists(directPath)) {
            return directPath;
        }

        Path dataPath = DATA_FOLDER.resolve(target);
        if (Files.exists(dataPath)) {
            return dataPath;
        }

        Path jsonPath = DATA_FOLDER.resolve(ensureJsonExtension(target));
        if (Files.exists(jsonPath)) {
            return jsonPath;
        }

        throw new NodeFileException("Invalid data file argument: " + target);
    }

    private String ensureJsonExtension(String target) {
        if (target.endsWith(".json")) {
            return target;
        }
        return target + ".json";
    }

    private boolean hasNoArguments(String[] args) {
        return args == null || args.length == 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isSupportedKey(String key) {
        return "config".equals(key) || "users".equals(key) || "tags".equals(key);
    }

    private record ParsedArguments(String config, String users, String tags, boolean useDefaults) {
        private static ParsedArguments defaults() {
            return new ParsedArguments(null, null, null, true);
        }
    }
}
