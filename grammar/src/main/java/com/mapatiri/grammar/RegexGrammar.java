package com.mapatiri.grammar;

import com.mapatiri.grammar.parse.*;

import java.util.*;
import java.util.function.Function;
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
    private final List<Parser<? extends ParsedCommand>> PARSERS = List.of(
            new Parser<>("SUBSCRIBE",   m -> new Subscribe(m.group(1))),
            new Parser<>("SEND",        m -> new Send(m.group(1))),
            new Parser<>("MSG",         m -> new Msg(m.group(1), m.group(2))),
            new Parser<>("POST",        m -> new Post(m.group(1), m.group(2), m.group(3), m.group(4))),
            new Parser<>("CHALLENGE",   m -> new Challenge(m.group(1), m.group(2))),
            new Parser<>("LEAVE",       m -> new Leave()),
            new Parser<>("ERROR",       m -> new ErrorProtocole(m.group(1))),
            new Parser<>("RESPONSE",    m -> new Response(m.group(1))),
            new Parser<>("USER_ACTION", m -> new UserAction(m.group(1))),
            new Parser<>("WELCOME",     m -> new Welcome(m.group(1), m.group(2))),
            new Parser<>("ONBOARDING",  m -> new Onboarding(m.group(1), m.group(2), m.group(3))),
            new Parser<>("OK",          m -> new Ok(m.group(1))),
            new Parser<>("ADVERT",      m -> new Advert(m.group(1), m.group(2)))
    );

    public RegexGrammar() {
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
        //TODO A suprimer
        regex.put("ID_AT_DOMAIN", Pattern.compile(ID_AT_DOMAIN));
        regex.put("USER_AT_DOMAIN", Pattern.compile(USER_AT_DOMAIN));
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

    public ParsedCommand parse(String command) {
        String trimmed = command.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("The command doesn't match any regex");
        }

        Matcher subMatcher = regex.get("SUBSCRIBE").matcher(trimmed);
        if (subMatcher.matches()) {
            return new Subscribe(subMatcher.group(1));
        }

        Matcher sendMatcher = regex.get("SEND").matcher(trimmed);
        if (sendMatcher.matches()) {
            return new Send(sendMatcher.group(1));
        }

        Matcher msgMatcher = regex.get("MSG").matcher(trimmed);
        if (msgMatcher.matches()) {
            return new Msg(msgMatcher.group(1), msgMatcher.group(2));
        }

        Matcher postMatcher = regex.get("POST").matcher(trimmed);
        if (postMatcher.matches()) {
            return new Post(
                    postMatcher.group(1),
                    postMatcher.group(2),
                    postMatcher.group(3),
                    postMatcher.group(4)
            );
        }

        Matcher challengeMatcher = regex.get("CHALLENGE").matcher(trimmed);
        if (challengeMatcher.matches()) {
            return new Challenge(challengeMatcher.group(1), challengeMatcher.group(2));
        }

        Matcher leaveMatcher = regex.get("LEAVE").matcher(trimmed);
        if (leaveMatcher.matches()) {
            return new Leave();
        }

        Matcher errorMatcher = regex.get("ERROR").matcher(trimmed);
        if (errorMatcher.matches()) {
            return new ErrorProtocole(errorMatcher.group(1));
        }

        Matcher responseMatcher = regex.get("RESPONSE").matcher(trimmed);
        if (responseMatcher.matches()) {
            return new Response(responseMatcher.group(1));
        }

        Matcher userActionMatcher = regex.get("USER_ACTION").matcher(trimmed);
        if (userActionMatcher.matches()) {
            return new UserAction(userActionMatcher.group(1));
        }

        Matcher welcomeMatcher = regex.get("WELCOME").matcher(trimmed);
        if (welcomeMatcher.matches()) {
            return new Welcome(welcomeMatcher.group(1), welcomeMatcher.group(2));
        }

        Matcher onboardingMatcher = regex.get("ONBOARDING").matcher(trimmed);
        if (onboardingMatcher.matches()) {
            return new Onboarding(
                    onboardingMatcher.group(1),
                    onboardingMatcher.group(2),
                    onboardingMatcher.group(3)
            );
        }

        Matcher okMatcher = regex.get("OK").matcher(trimmed);
        if (okMatcher.matches()) {
            return new Ok(okMatcher.group(1));
        }

        Matcher advertMatcher = regex.get("ADVERT").matcher(trimmed);
        if (advertMatcher.matches()) {
            return new Advert(advertMatcher.group(1), advertMatcher.group(2));
        }

        throw new IllegalArgumentException("Unknown command: " + trimmed);
    }

    public String build(ParsedCommand command) {
        return switch (command) {
            case Send s ->
                    validate("SEND", "SEND " + s.content() + "\r\n");
            case Subscribe s ->
                    validate("SUBSCRIBE", "SUBSCRIBE " + s.target() + "\r\n");
            case Post p ->
                    validate("POST", "POST " + p.id() + " " + p.sender() + " " + p.receiver() + " " + p.content() + "\r\n");
            case Msg m ->
                    validate("MSG", "MSG " + m.sender() + " " + m.content() + "\r\n");
            case Leave l ->
                    "LEAVE\r\n";
            case Challenge c ->
                    validate("CHALLENGE", "CHALLENGE " + c.counter() + " " + c.randomString() + "\r\n");
            case Response r ->
                    validate("RESPONSE", "RESPONSE " + r.sha3hex() + "\r\n");
            case Onboarding o ->
                    validate("ONBOARDING", "ONBOARDING " + o.user() + " " + o.saltLength() + " " + o.bcryptHash() + "\r\n");
            case UserAction u ->
                    validate("USER_ACTION", "USER " + u.login() + "\r\n");
            case Ok o ->
                    validate("OK", "OK " + o.message() + "\r\n");
            case ErrorProtocole e ->
                    validate("ERROR", "ERROR " + e.message() + "\r\n");
            case Advert a ->
                    validate("ADVERT", "ADVERT " + a.port() + " " + a.domain() + "\r\n");
            case Welcome w ->
                    validate("WELCOME", "WELCOME " + w.domain() + " " + w.randomString() + "\r\n");
        };
    }

    private String validate(String type, String message) {
        Matcher m = regex.get(type).matcher(message);
        if (!m.matches()) throw new IllegalArgumentException("Invalid: " + message);
        return message;
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

    public List<String> extractTagsFromMessage(String text) {
        Pattern tagPattern = Pattern.compile(TAG);
        Matcher m = tagPattern.matcher(text);

        List<String> tags = new ArrayList<>();
        while (m.find()) {
            tags.add(m.group());
        }
        return tags;
    }

    public String generateIdAtDomain(String userAtDomain) {
        String domain = userAtDomain.substring(userAtDomain.indexOf('@') + 1);

        // je peux pas utiliser UUUID car il faut que je precise le mas de nbr que je souhaite
        String id = String.valueOf((int) (Math.random() * 99999) + 1);

        String result = id + "@" + domain;

        Matcher matcher = regex.get("ID_AT_DOMAIN").matcher(result);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Generated ID does not match ID_AT_DOMAIN pattern");
        }

        return result;
    }

}