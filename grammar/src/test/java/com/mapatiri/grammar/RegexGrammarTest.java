package com.mapatiri.grammar;

import com.mapatiri.grammar.parse.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegexGrammarTest {

    private RegexGrammar grammar;

    @BeforeEach
    void setUp() {
        grammar = new RegexGrammar();
    }

    // ─────────────────────────────────────────────
    // parse() — happy paths
    // ─────────────────────────────────────────────

    @Nested
    class Parse {

        @Test
        void parsesSubscribeWithUser() {
            ParsedCommand cmd = grammar.parse("SUBSCRIBE user1@example.com\r\n");
            assertInstanceOf(Subscribe.class, cmd);
            assertEquals("user1@example.com", ((Subscribe) cmd).target());
        }

        @Test
        void parsesSubscribeWithTag() {
            ParsedCommand cmd = grammar.parse("SUBSCRIBE #tag12@example.com\r\n");
            assertInstanceOf(Subscribe.class, cmd);
            assertEquals("#tag12@example.com", ((Subscribe) cmd).target());
        }

        @Test
        void parsesSend() {
            ParsedCommand cmd = grammar.parse("SEND Hello world\r\n");
            assertInstanceOf(Send.class, cmd);
            assertEquals("Hello world", ((Send) cmd).content());
        }

        @Test
        void parsesMsg() {
            ParsedCommand cmd = grammar.parse("MSG user1@example.com Hello!\r\n");
            assertInstanceOf(Msg.class, cmd);
            assertEquals("user1@example.com", ((Msg) cmd).sender());
            assertEquals("Hello!", ((Msg) cmd).content());
        }

        @Test
        void parsesLeave() {
            ParsedCommand cmd = grammar.parse("LEAVE\r\n");
            assertInstanceOf(Leave.class, cmd);
        }

        @Test
        void parsesOk() {
            ParsedCommand cmd = grammar.parse("OK Welcome to Cacophony\r\n");
            assertInstanceOf(Ok.class, cmd);
            assertEquals("Welcome to Cacophony", ((Ok) cmd).message());
        }

        @Test
        void parsesError() {
            ParsedCommand cmd = grammar.parse("ERROR Invalid credentials\r\n");
            assertInstanceOf(ErrorProtocole.class, cmd);
            assertEquals("Invalid credentials", ((ErrorProtocole) cmd).message());
        }

        @Test
        void parsesWelcome() {
            String random22 = "abcABC123!@#deGHI456$%"; // exactly 22 chars
            ParsedCommand cmd = grammar.parse("WELCOME example.com " + random22 + "\r\n");
            assertInstanceOf(Welcome.class, cmd);
            assertEquals("example.com", ((Welcome) cmd).domain());
            assertEquals(random22, ((Welcome) cmd).randomString());
        }

        @Test
        void parsesChallenge() {
            ParsedCommand cmd = grammar.parse("CHALLENGE 12 abcABC123!@#deGHI456$%\r\n");
            assertInstanceOf(Challenge.class, cmd);
            assertEquals("12", ((Challenge) cmd).counter());
        }

        @Test
        void parsesResponse() {
            String hex = "a".repeat(64);
            ParsedCommand cmd = grammar.parse("RESPONSE " + hex + "\r\n");
            assertInstanceOf(Response.class, cmd);
            assertEquals(hex, ((Response) cmd).sha3hex());
        }

        @Test
        void parsesUserAction() {
            ParsedCommand cmd = grammar.parse("USER user1\r\n");
            assertInstanceOf(UserAction.class, cmd);
            assertEquals("user1", ((UserAction) cmd).login());
        }

        @Test
        void parsesOnboarding() {
            String bcrypt = "$2b$12$abcABC123defGHI456jklM";
            ParsedCommand cmd = grammar.parse("ONBOARDING user1 12 " + bcrypt + "\r\n");
            assertInstanceOf(Onboarding.class, cmd);
            assertEquals("user1", ((Onboarding) cmd).user());
            assertEquals("12", ((Onboarding) cmd).saltLength());
            assertEquals(bcrypt, ((Onboarding) cmd).bcryptHash());
        }

        @Test
        void parsesAdvert() {
            ParsedCommand cmd = grammar.parse("ADVERT 8080 example.com\r\n");
            assertInstanceOf(Advert.class, cmd);
            assertEquals("8080", ((Advert) cmd).port());
            assertEquals("example.com", ((Advert) cmd).domain());
        }

        @Test
        void parsesPost() {
            ParsedCommand cmd = grammar.parse("POST 42@example.com user1@example.com user2@example.com Hello!\r\n");
            assertInstanceOf(Post.class, cmd);
            assertEquals("42@example.com", ((Post) cmd).id());
            assertEquals("user1@example.com", ((Post) cmd).sender());
            assertEquals("user2@example.com", ((Post) cmd).receiver());
            assertEquals("Hello!", ((Post) cmd).content());
        }

        @Test
        void trimsCommandBeforeParsing() {
            ParsedCommand cmd = grammar.parse("   LEAVE\r\n   ");
            assertInstanceOf(Leave.class, cmd);
        }
    }

    // ─────────────────────────────────────────────
    // parse() — error paths
    // ─────────────────────────────────────────────

    @Nested
    class ParseErrors {

        @Test
        void throwsOnBlankCommand() {
            assertThrows(IllegalArgumentException.class, () -> grammar.parse("   "));
        }

        @Test
        void throwsOnUnknownCommand() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> grammar.parse("UNKNOWN something\r\n")
            );
            assertTrue(ex.getMessage().contains("Unknown command"));
        }

        @Test
        void throwsOnSendWithoutContent() {
            assertThrows(IllegalArgumentException.class, () -> grammar.parse("SEND\r\n"));
        }

        @Test
        void throwsOnMsgWithoutContent() {
            assertThrows(IllegalArgumentException.class, () -> grammar.parse("MSG user1\r\n"));
        }

        @Test
        void throwsOnSubscribeWithoutTarget() {
            assertThrows(IllegalArgumentException.class, () -> grammar.parse("SUBSCRIBE \r\n"));
        }
    }

    // ─────────────────────────────────────────────
    // build()
    // ─────────────────────────────────────────────

    @Nested
    class Build {

        @Test
        void buildsAndRoundtripsSubscribe() {
            Subscribe sub = new Subscribe("user1@example.com");
            assertEquals(sub, grammar.parse(grammar.build(sub)));
        }

        @Test
        void buildsAndRoundtripsSend() {
            Send send = new Send("Hello world");
            assertEquals(send, grammar.parse(grammar.build(send)));
        }

        @Test
        void buildsAndRoundtripsOk() {
            Ok ok = new Ok("Welcome back");
            assertEquals(ok, grammar.parse(grammar.build(ok)));
        }

        @Test
        void buildsLeave() {
            assertEquals("LEAVE\r\n", grammar.build(new Leave()));
        }

        @Test
        void throwsOnContentTooLong() {
            assertThrows(IllegalArgumentException.class,
                    () -> grammar.build(new Send("x".repeat(201))));
        }
    }

    // ─────────────────────────────────────────────
    // extractTagsFromMessage()
    // ─────────────────────────────────────────────

    @Nested
    class ExtractTags {

        @Test
        void extractsSingleTag() {
            List<String> tags = grammar.extractTagsFromMessage("Hello #tag12 how are you");
            assertEquals(1, tags.size());
            assertEquals("#tag12", tags.getFirst());
        }

        @Test
        void extractsMultipleTags() {
            List<String> tags = grammar.extractTagsFromMessage("#tag12 and #tag99 here");
            assertEquals(2, tags.size());
            assertEquals("#tag12", tags.get(0));
            assertEquals("#tag99", tags.get(1));
        }

        @Test
        void returnsEmptyWhenNoTags() {
            assertTrue(grammar.extractTagsFromMessage("no tags here").isEmpty());
        }
    }

    // ─────────────────────────────────────────────
    // generateIdAtDomain()
    // ─────────────────────────────────────────────

    @Nested
    class GenerateIdAtDomain {

        @Test
        void preservesDomain() {
            String result = grammar.generateIdAtDomain("user1@example.com");
            assertTrue(result.endsWith("@example.com"));
        }

        @Test
        void generatesValidFormat() {
            String result = grammar.generateIdAtDomain("user1@example.com");
            assertTrue(result.matches("\\d{1,5}@(?:[0-9A-Za-z]|\\.){5,200}"));
        }

        @Test
        void generatesDistinctIds() {
            long distinct = java.util.stream.IntStream.range(0, 20)
                    .mapToObj(i -> grammar.generateIdAtDomain("user1@example.com"))
                    .map(s -> s.substring(0, s.indexOf('@')))
                    .distinct()
                    .count();
            assertTrue(distinct > 1);
        }
    }
}