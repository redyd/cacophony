package com.mapatiri.grammar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegexGrammarTest {
    String w = "WELCOME exampledomain abcDEF1234567890!@#$%^";
    String u = "USER user12345";
    String c = "CHALLENGE 05 ZYXabc1234567890!@#$%^";
    String r = "RESPONSE abcdef1234567890abcdef1234567890abcdef";
    String o = "ONBOARDING user12345 12 $2b$05$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcd";
    String s = "SUBSCRIBE user12345@exampledomain";
    String se = "SEND Hello this is a test message";
    String m = "MSG user12345@exampledomain Hello this is a received message";
    String ok = "OK Command successful";
    String e = "ERROR Something went wrong";
    String l = "LEAVE";
    String ad = "ADVERT 8080 exampledomain";
    String p = "POST 123@exampledomain user12345@exampledomain user67890@exampledomain This is a longer inner message used for testing the post command";


    @Test
    public void should_return_the_right_type_of_command(){
        RegexGrammar ww = new RegexGrammar(w);

        assertEquals("WELCOME", ww.getTypeOfCommand());
    }
}
