package com.mapatiri.grammar;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexGrammar {
    private final String SP = " ";
    private final String NEWLINE = "(?:\\r\\n)?";
    private final String DIGIT = "[0-9]";
    private final String LETTER = "[A-Za-z]";
    private final String PORT = DIGIT + "{1,5}";
    private final String COUNTER = DIGIT + "{2}";
    private final String SALT_LENGTH = DIGIT + "{2}";
    private final String DIGIT_LETTER = "[0-9A-Za-z]";
    private final String CHARACTER = "[\\x20-\\xFF]";
    private final String SPECIAL_CHAR = "[!-/:-@\\[-`]";
    private final String MESSAGE = CHARACTER + "{1,200}";
    private final String INNER_MESSAGE = CHARACTER + "{1,500}";
    private final String USER = DIGIT_LETTER + "{5,20}";
    private final String TAG = "#" + DIGIT_LETTER + "{5,20}";
    private final String DOMAIN = "(?:" + DIGIT_LETTER + "|\\.){5,200}";
    private final String USER_AT_DOMAIN = USER + "@" + DOMAIN;
    private final String TAG_AT_DOMAIN = TAG + "@" + DOMAIN;
    private final String ID_AT_DOMAIN = DIGIT + "{1,5}@" + DOMAIN;
    private final String BCRYPT = "\\$2b\\$" + COUNTER + "\\$(?:" + DIGIT_LETTER + "|" + SPECIAL_CHAR + "){1,70}";
    private final String SHA3_HEXSTRING = DIGIT_LETTER + "{30,200}";
    private final String RANDOM_STRING = "(?:" + DIGIT_LETTER + "|" + SPECIAL_CHAR + "){22}";
    private final String WELCOME = "WELCOME" + SP + "(" + DOMAIN + ")" + SP + "(" + RANDOM_STRING + ")" + NEWLINE;
    private final String USER_ACTION = "USER" + SP + "(" + USER + ")" + NEWLINE;
    private final String CHALLENGE = "CHALLENGE" + SP + "(" + COUNTER + ")" + SP + "(" + RANDOM_STRING + ")" + NEWLINE;
    private final String RESPONSE = "RESPONSE" + SP + "(" + SHA3_HEXSTRING + ")" + NEWLINE;
    private final String ONBOARDING = "ONBOARDING" + SP + "(" + USER + ")" + SP + "(" + SALT_LENGTH + ")" + SP + "(" + BCRYPT + ")" + NEWLINE;
    private final String SUBSCRIBE = "SUBSCRIBE" + SP + "(" + USER_AT_DOMAIN + "|" + TAG_AT_DOMAIN + ")" + NEWLINE;
    private final String SEND = "SEND" + SP + "(" + MESSAGE + ")" + NEWLINE;
    private final String MSG = "MSG" + SP + "(" + USER_AT_DOMAIN + ")" + SP + "(" + MESSAGE + ")" + NEWLINE;
    private final String OK = "OK" + SP + "(" + MESSAGE + ")" + NEWLINE;
    private final String ERROR = "ERROR" + SP + "(" + MESSAGE + ")" + NEWLINE;
    private final String LEAVE = "LEAVE" + NEWLINE;
    private final String ADVERT = "ADVERT" + SP + "(" + PORT + ")" + SP + "(" + DOMAIN + ")" + NEWLINE;
    private final String POST = "POST" + SP + "(" + ID_AT_DOMAIN + ")" + SP + "(" + USER_AT_DOMAIN + ")" + SP + "(" + USER_AT_DOMAIN + "|" + TAG_AT_DOMAIN + ")" + SP + "(" + INNER_MESSAGE + ")" + NEWLINE;

    private final String command;
    private final Map<String, Pattern> regex = new HashMap<>();

    public RegexGrammar(){
        this("");
    }

    public RegexGrammar(String command) {
        this.command = command.trim();
        regex.put("WELCOME", Pattern.compile(WELCOME));
        regex.put("USER_ACTION", Pattern.compile(USER_ACTION));
        regex.put("CHALLENGE", Pattern.compile(CHALLENGE));
        regex.put("RESPONSE", Pattern.compile(RESPONSE));
        regex.put("ONBOARDING", Pattern.compile(ONBOARDING));
        regex.put("SUBSCRIBE", Pattern.compile(SUBSCRIBE));
        regex.put("SEND", Pattern.compile(SEND));
        regex.put("MSG", Pattern.compile(MSG));
        regex.put("OK", Pattern.compile(OK));
        regex.put("ERROR", Pattern.compile(ERROR));
        regex.put("LEAVE", Pattern.compile(LEAVE));
        regex.put("ADVERT", Pattern.compile(ADVERT));
        regex.put("POST", Pattern.compile(POST));
    }

    public List<String> parseCommand() {
        String type = getTypeOfCommand();
        if (type.isBlank()) {
            throw new IllegalArgumentException("The command doesn't match any regex");
        }
        Matcher matcher = regex.get(type).matcher(command);
        int groupCount = matcher.groupCount();
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Command does not match");
        }
        List<String> elements = new ArrayList<>();
        for (int i = 1; i <= groupCount; i++) {
            elements.add(matcher.group(i));
        }
        return elements;
    }

    public String getTypeOfCommand() {
        Set<String> keys = regex.keySet();
        for (String key : keys) {
            Pattern pattern = regex.get(key);
            Matcher matcher = pattern.matcher(command);
            if (matcher.matches()) {
                return key;
            }
        }
        return "";
    }

    public String buildCommand(String typeOfCommand, String... parts) {
        Pattern pattern = regex.get(typeOfCommand);
        List<String> tabMessage = new ArrayList<>(List.of(typeOfCommand));
        tabMessage.addAll(List.of(parts));
        String message = String.join(" ", tabMessage);
        message = message.trim();
        message += "\r\n";
        Matcher matcher = pattern.matcher(message);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Arguments don't match the command pattern");
        }
        return message;
    }

    public List<String> extractTagsFromMessage() {
        String type = getTypeOfCommand();
        if (!"MSG".equals(type) && !"POST".equals(type) ) {
            throw new IllegalArgumentException("Only the MSG and the POST type is accepted");
        }
        List<String> parts = parseCommand();

        String text;
        // on prend seulement la partie MESSAGE
        if ("MSG".equals(type)) {
            text = parts.get(1);
        } else { // POST
            text = parts.get(3);
        }

        // on utilise le regex TAG
        Pattern tagPattern = Pattern.compile(TAG);
        Matcher m = tagPattern.matcher(text);

        List<String> tags = new ArrayList<>();
        while (m.find()) {
            tags.add(m.group()); // le group retourne la valeur apres que le regex a été appliqué
        }
        return tags;
    }

}