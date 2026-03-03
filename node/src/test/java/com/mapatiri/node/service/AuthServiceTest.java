package com.mapatiri.node.service;

import com.mapatiri.node.model.BcryptParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    void testParseValidBcrypt() {
        // Énoncé TP : salt_length=22
        String bcrypt = "$2b$14$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLmnopqrstuvwxyzABCDEF";
        BcryptParams params = authService.parseByCrypt(bcrypt);

        assertEquals(14, params.rotationNbr());
        assertEquals("abcdefghijklmnopqrstuv", params.saltLenght());
        assertEquals(22, params.saltLenght().length());
    }

}