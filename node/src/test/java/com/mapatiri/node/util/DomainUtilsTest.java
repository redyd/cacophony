package com.mapatiri.node.util;

    // ==========================================
// DomainUtilsTest.java


import org.junit.jupiter.api.Test;
import util.DomainUtils;

import static org.junit.jupiter.api.Assertions.*;

    class DomainUtilsTest {

        // ===== extractDomain =====

        @Test
        void extractDomain_userAtDomain_returnsDomain() {
            assertEquals("node1.swilabus.xyz", DomainUtils.extractDomain("louis@node1.swilabus.xyz"));
        }

        @Test
        void extractDomain_tagAtDomain_returnsDomain() {
            assertEquals("node1.swilabus.xyz", DomainUtils.extractDomain("#metal@node1.swilabus.xyz"));
        }

        @Test
        void extractDomain_shortDomain_returnsDomain() {
            assertEquals("node1.xyz", DomainUtils.extractDomain("louis@node1.xyz"));
        }

        // ===== isDistantDomain =====

        @Test
        void isDistantDomain_differentDomains_returnsTrue() {
            assertTrue(DomainUtils.isDistantDomain(
                    "alice@node2.swilabus.xyz",
                    "louis@node1.swilabus.xyz"
            ));
        }

        @Test
        void isDistantDomain_sameDomain_returnsFalse() {
            assertFalse(DomainUtils.isDistantDomain(
                    "alice@node1.swilabus.xyz",
                    "louis@node1.swilabus.xyz"
            ));
        }

        @Test
        void isDistantDomain_tagDistantDomain_returnsTrue() {
            assertTrue(DomainUtils.isDistantDomain(
                    "#metal@node2.swilabus.xyz",
                    "louis@node1.swilabus.xyz"
            ));
        }

        @Test
        void isDistantDomain_tagSameDomain_returnsFalse() {
            assertFalse(DomainUtils.isDistantDomain(
                    "#metal@node1.swilabus.xyz",
                    "louis@node1.swilabus.xyz"
            ));
        }

        @Test
        void isDistantDomain_bothJustDomains_returnsFalse() {
            assertFalse(DomainUtils.isDistantDomain(
                    "node1.swilabus.xyz",
                    "node1.swilabus.xyz"
            ));
        }

        @Test
        void isDistantDomain_bothJustDomains_returnsTrue() {
            assertTrue(DomainUtils.isDistantDomain(
                    "node2.swilabus.xyz",
                    "node1.swilabus.xyz"
            ));
        }
    }

