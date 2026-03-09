package com.mapatiri.grammar;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class RegexGrammarTest {
    String w = "WELCOME exampledomain abcDEF1234567890!@#$%^\r\n";
    String u = "USER user12345\r\n";
    String c = "CHALLENGE 05 ZYXabc1234567890!@#$%^\r\n";
    String r = "RESPONSE abcdef1234567890abcdef1234567890abcdef\r\n";
    String o = "ONBOARDING user12345 12 $2b$05$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcd\r\n";
    String s = "SUBSCRIBE user12345@exampledomain\r\n";
    String se = "SEND Hello this is a test message\r\n";
    String m = "MSG user12345@exampledomain Hello this is a received message\r\n";
    String ok = "OK Command successful\r\n";
    String e = "ERROR Something went wrong\r\n";
    String l = "LEAVE\r\n";
    String ad = "ADVERT 8080 exampledomain\r\n";
    String p = "POST 123@exampledomain user12345@exampledomain user67890@exampledomain This is a longer inner message used for testing the post command\r\n";

    RegexGrammar ww = new RegexGrammar(w);
    RegexGrammar uu = new RegexGrammar(u);
    RegexGrammar cc = new RegexGrammar(c);
    RegexGrammar rr = new RegexGrammar(r);
    RegexGrammar oo = new RegexGrammar(o);
    RegexGrammar ss = new RegexGrammar(s);
    RegexGrammar see = new RegexGrammar(se);
    RegexGrammar mm = new RegexGrammar(m);
    RegexGrammar okk = new RegexGrammar(ok);
    RegexGrammar ee = new RegexGrammar(e);
    RegexGrammar ll = new RegexGrammar(l);
    RegexGrammar add = new RegexGrammar(ad);
    RegexGrammar pp = new RegexGrammar(p);

    @Test
    public void should_return_the_right_type_of_command(){

        assertEquals("WELCOME", ww.getTypeOfCommand());
        assertEquals("USER_ACTION", uu.getTypeOfCommand());
        assertEquals("CHALLENGE", cc.getTypeOfCommand());
        assertEquals("RESPONSE", rr.getTypeOfCommand());
        assertEquals("ONBOARDING", oo.getTypeOfCommand());
        assertEquals("SUBSCRIBE", ss.getTypeOfCommand());
        assertEquals("SEND", see.getTypeOfCommand());
        assertEquals("MSG", mm.getTypeOfCommand());
        assertEquals("OK", okk.getTypeOfCommand());
        assertEquals("ERROR", ee.getTypeOfCommand());
        assertEquals("LEAVE", ll.getTypeOfCommand());
        assertEquals("ADVERT", add.getTypeOfCommand());
        assertEquals("POST", pp.getTypeOfCommand());
    }

    @Test
    public void should_parse_correctly(){
        String[] welcome = {"exampledomain", "abcDEF1234567890!@#$%^"};
        String[] user = {"user12345"};
        String[] challenge = {"05", "ZYXabc1234567890!@#$%^"};
        String[] response = {"abcdef1234567890abcdef1234567890abcdef"};
        String[] onboarding = {
                "user12345",
                "12",
                "$2b$05$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcd"
        };
        String[] subscribe = {"user12345@exampledomain"};
        String[] send = {"Hello this is a test message"};
        String[] msg = {"user12345@exampledomain", "Hello this is a received message"};
        String[] ok = {"Command successful"};
        String[] error = {"Something went wrong"};
        String[] leave = {};
        String[] advert = {"8080", "exampledomain"};
        String[] post = {
                "123@exampledomain",
                "user12345@exampledomain",
                "user67890@exampledomain",
                "This is a longer inner message used for testing the post command"
        };
        assertArrayEquals(welcome, ww.parseCommand().toArray());
        assertArrayEquals(user, uu.parseCommand().toArray());
        assertArrayEquals(challenge, cc.parseCommand().toArray());
        assertArrayEquals(response, rr.parseCommand().toArray());
        assertArrayEquals(onboarding, oo.parseCommand().toArray());
        assertArrayEquals(subscribe, ss.parseCommand().toArray());
        assertArrayEquals(send, see.parseCommand().toArray());
        assertArrayEquals(msg, mm.parseCommand().toArray());
        assertArrayEquals(ok, okk.parseCommand().toArray());
        assertArrayEquals(error, ee.parseCommand().toArray());
        assertArrayEquals(leave, ll.parseCommand().toArray());
        assertArrayEquals(advert, add.parseCommand().toArray());
        assertArrayEquals(post, pp.parseCommand().toArray());
    }

    @Test
    public void should_build_the_command_correctly(){
        RegexGrammar none = new RegexGrammar();

        String noneParse1 = none.buildCommand("WELCOME", "exampledomain", "abcDEF1234567890!@#$%^");
        String noneParse2 = none.buildCommand("CHALLENGE", "05", "ZYXabc1234567890!@#$%^");
        String noneParse3 = none.buildCommand("ONBOARDING", "user12345", "12", "$2b$05$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcd");
        String noneParse4 = none.buildCommand("LEAVE", "");

        assertEquals(w, noneParse1);
        assertEquals(c, noneParse2);
        assertEquals(o, noneParse3);
        assertEquals(l, noneParse4);
    }
}
