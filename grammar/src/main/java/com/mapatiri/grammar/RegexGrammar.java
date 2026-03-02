package com.mapatiri.grammar;

public class RegexGrammar {
    private final String SP = " ";
    private final String NEWLINE = "\r\n";
    private final String DIGIT = "[0-9]";
    private final String LETTER = "[A-Za-z]";
    private final String PORT = "^" + DIGIT + "{1,5}$";
    private final String COUNTER = "^" + DIGIT + "{2}$";
    private final String SALT_LENGTH = "^" + DIGIT + "{2}$";
    private final String DIGIT_LETTER = DIGIT + "|" + LETTER;
    private final String CHARACTER = ".";
    private final String SPECIAL_CHAR = "[!-/]|[:-@]|[\\[-`]";
    private final String MESSAGE = "^" + CHARACTER + "{1-200}$";
    private final String INNER_MESSAGE = "^" + CHARACTER + "{1-500}";
    private final String USER = "^"+DIGIT_LETTER+"{5-20}";
    private final String TAG= "#"+DIGIT_LETTER+"{5-20}";
    private final String DOMAIN= "("+DIGIT_LETTER+"|\\.){5-200}";
    private final String USER_AT_DOMAIN= USER+"@"+DOMAIN;
    private final String TAG_AT_DOMAIN= TAG+"@"+DOMAIN;
    private final String ID_AT_DOMAIN=DIGIT+"{1,5}@"+DOMAIN;
    private final String BCRYPT= "$2b$"+COUNTER+"$("+DIGIT_LETTER+"|"+SPECIAL_CHAR+"{1-70}";
    private final String SHA3_HEXSTRING= DIGIT_LETTER+"{30-200}";
    private final String RANDOM_STRING= "("+DIGIT_LETTER+"|"+SPECIAL_CHAR+"{22})";
    private final String WELCOME = "WELCOME" + SP + DOMAIN + SP + RANDOM_STRING + NEWLINE;
    private final String USER_ACTION = "USER" + SP + USER + NEWLINE;
    private final String CHALLENGE = "CHALLENGE" + SP + COUNTER + SP + RANDOM_STRING + NEWLINE;
    private final String RESPONSE = "RESPONSE" + SP + SHA3_HEXSTRING + NEWLINE;
    private final String ONBOARDING = "ONBOARDING" + SP + USER + SP + SALT_LENGTH + SP + BCRYPT + NEWLINE;
    private final String SUBSCRIBE = "SUBSCRIBE" + SP + "(" + USER_AT_DOMAIN + "|" + TAG_AT_DOMAIN + ")" + NEWLINE;
    private final String SEND = "SEND" + SP + MESSAGE + NEWLINE;
    private final String MSG = "MSG" + SP + USER_AT_DOMAIN + SP + MESSAGE + NEWLINE;
    private final String OK = "OK" + "(" + SP + MESSAGE + ")*" + NEWLINE;
    private final String ERROR = "ERROR" + "(" + SP + MESSAGE + ")*" + NEWLINE;
    private final String LEAVE = "LEAVE" + NEWLINE;
    private final String ADVERT = "ADVERT" + SP + PORT + SP + DOMAIN + NEWLINE;
    private final String POST = "POST" + SP + ID_AT_DOMAIN + SP + USER_AT_DOMAIN + SP + "(" + USER_AT_DOMAIN + "|" + TAG_AT_DOMAIN + ")" + SP + INNER_MESSAGE + NEWLINE;
}
