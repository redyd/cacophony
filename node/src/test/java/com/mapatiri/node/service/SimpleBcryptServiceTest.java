package com.mapatiri.node.service;

import com.mapatiri.node.model.BcryptParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleBcryptServiceTest {

    SimpleBcryptService simpleBcryptService;

    @BeforeEach
    void setUp() {
        simpleBcryptService = new SimpleBcryptService();
    }

    @Test
    void testParseValidBcrypt() {
        String bcrypt = "$2b$14$mysaltmyhash";
        BcryptParams params = simpleBcryptService.parseByCrypt(bcrypt, 6);

        assertEquals(14, params.round());
        assertEquals("mysalt", params.salt());
        assertEquals("myhash", params.hash());
    }

}