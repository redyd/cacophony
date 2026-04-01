package com.mapatiri.node.service;

import com.mapatiri.node.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdentityUtilsTest {

    private IdentityUtils identityUtils;

    private static final String LOCAL_DOMAIN = "node1.swilabus.xyz";
    private static final String DISTANT_DOMAIN = "node2.swilabus.xyz";
    private static final String SENDER = "louis@" + LOCAL_DOMAIN;

    @BeforeEach
    void setUp() {
        identityUtils = new IdentityUtils();
    }

    // ===================== stripDomain =====================

    @Test
    void stripDomain_withAtSign_returnsLoginOnly() {
        assertEquals("louis", identityUtils.stripDomain("louis@node1.swilabus.xyz"));
    }

    @Test
    void stripDomain_withoutAtSign_returnsInputUnchanged() {
        assertEquals("louis", identityUtils.stripDomain("louis"));
    }

    @Test
    void stripDomain_tagWithDomain_returnsTagOnly() {
        assertEquals("#monTag1", identityUtils.stripDomain("#monTag1@node1.swilabus.xyz"));
    }

    // ===================== extractDomain =====================

    @Test
    void extractDomain_validInput_returnsDomain() {
        assertEquals(LOCAL_DOMAIN, identityUtils.extractDomain(SENDER));
    }

    @Test
    void extractDomain_tagAtDomain_returnsDomain() {
        assertEquals(LOCAL_DOMAIN, identityUtils.extractDomain("#monTag1@node1.swilabus.xyz"));
    }

    // ===================== isDistantDomain =====================

    @Test
    void isDistantDomain_sameDomain_returnsFalse() {
        assertFalse(identityUtils.isDistantDomain("alice@node1.swilabus.xyz", SENDER));
    }

    @Test
    void isDistantDomain_differentDomain_returnsTrue() {
        assertTrue(identityUtils.isDistantDomain("alice@node2.swilabus.xyz", SENDER));
    }

    @Test
    void isDistantDomain_bothAreDomainStrings_comparesDirectly() {
        assertFalse(identityUtils.isDistantDomain(LOCAL_DOMAIN, LOCAL_DOMAIN));
        assertTrue(identityUtils.isDistantDomain(DISTANT_DOMAIN, LOCAL_DOMAIN));
    }

    @Test
    void isDistantDomain_targetWithoutAt_usesTargetAsIs() {
        assertFalse(identityUtils.isDistantDomain(LOCAL_DOMAIN, SENDER));
    }

    // ===================== getLogin =====================

    @Test
    void getLogin_validInput_returnsLogin() {
        assertEquals("louis", identityUtils.getLogin(SENDER));
    }

    @Test
    void getLogin_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                identityUtils.getLogin(null)
        );
    }

    @Test
    void getLogin_noAtSign_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                identityUtils.getLogin("louis")
        );
    }

    // ===================== filterSubscribers =====================

    @Test
    void filterSubscribers_local_returnsOnlyLocalSubscribers() {
        Set<String> subscribers = Set.of(
                "alice@" + LOCAL_DOMAIN,
                "bob@" + DISTANT_DOMAIN
        );

        Set<String> result = identityUtils.filterSubscribers(subscribers, SENDER, "louis", false);

        assertTrue(result.contains("alice"));
        assertFalse(result.contains("bob@" + DISTANT_DOMAIN));
    }

    @Test
    void filterSubscribers_distant_returnsOnlyDistantSubscribers() {
        Set<String> subscribers = Set.of(
                "alice@" + LOCAL_DOMAIN,
                "bob@" + DISTANT_DOMAIN
        );

        Set<String> result = identityUtils.filterSubscribers(subscribers, SENDER, "louis", true);

        assertTrue(result.contains("bob@" + DISTANT_DOMAIN));
        assertFalse(result.contains("alice"));
    }

    @Test
    void filterSubscribers_excludesSenderFromResults() {
        Set<String> subscribers = Set.of(
                "louis@" + LOCAL_DOMAIN,
                "alice@" + LOCAL_DOMAIN
        );

        Set<String> result = identityUtils.filterSubscribers(subscribers, SENDER, "louis", false);

        assertFalse(result.contains("louis"));
        assertTrue(result.contains("alice"));
    }

    @Test
    void filterSubscribers_local_stripsDomainsFromResults() {
        Set<String> subscribers = Set.of("alice@" + LOCAL_DOMAIN);

        Set<String> result = identityUtils.filterSubscribers(subscribers, SENDER, "louis", false);

        assertTrue(result.contains("alice"));
        assertFalse(result.contains("alice@" + LOCAL_DOMAIN));
    }

    @Test
    void filterSubscribers_distant_keepsDomainsInResults() {
        Set<String> subscribers = Set.of("bob@" + DISTANT_DOMAIN);

        Set<String> result = identityUtils.filterSubscribers(subscribers, SENDER, "louis", true);

        assertTrue(result.contains("bob@" + DISTANT_DOMAIN));
    }

    @Test
    void filterSubscribers_emptySubscribers_returnsEmpty() {
        Set<String> result = identityUtils.filterSubscribers(Set.of(), SENDER, "louis", false);
        assertTrue(result.isEmpty());
    }

    // ===================== filterTagSubscribers =====================

    @Test
    void filterTagSubscribers_matchingTag_returnsLocalSubscribers() {
        Tag tag = new Tag("#monTag1", LOCAL_DOMAIN);
        tag.addSubscriber("alice@" + LOCAL_DOMAIN);

        Set<String> result = identityUtils.filterTagSubscribers(
                List.of(tag), List.of("#monTag1"), SENDER, "louis", false);

        assertTrue(result.contains("alice"));
    }

    @Test
    void filterTagSubscribers_noMatchingTag_returnsEmpty() {
        Tag tag = new Tag("#monTag1", LOCAL_DOMAIN);
        tag.addSubscriber("alice@" + LOCAL_DOMAIN);

        Set<String> result = identityUtils.filterTagSubscribers(
                List.of(tag), List.of("#monTag2"), SENDER, "louis", false);

        assertTrue(result.isEmpty());
    }

    @Test
    void filterTagSubscribers_distant_returnsDistantSubscribersOnly() {
        Tag tag = new Tag("#monTag1", LOCAL_DOMAIN);
        tag.addSubscriber("alice@" + LOCAL_DOMAIN);
        tag.addSubscriber("bob@" + DISTANT_DOMAIN);

        Set<String> result = identityUtils.filterTagSubscribers(
                List.of(tag), List.of("#monTag1"), SENDER, "louis", true);

        assertTrue(result.contains("bob@" + DISTANT_DOMAIN));
        assertFalse(result.contains("alice"));
    }

    @Test
    void filterTagSubscribers_excludesSender() {
        Tag tag = new Tag("#monTag1", LOCAL_DOMAIN);
        tag.addSubscriber(SENDER); // sender lui-même
        tag.addSubscriber("alice@" + LOCAL_DOMAIN);

        Set<String> result = identityUtils.filterTagSubscribers(
                List.of(tag), List.of("#monTag1"), SENDER, "louis", false);

        assertFalse(result.contains("louis"));
        assertTrue(result.contains("alice"));
    }

    @Test
    void filterTagSubscribers_emptyTagList_returnsEmpty() {
        Set<String> result = identityUtils.filterTagSubscribers(
                List.of(), List.of("#monTag1"), SENDER, "louis", false);

        assertTrue(result.isEmpty());
    }
}