package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.repository.UserRepo;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginTaskTest {

    @Test
    void authenticateThrowsWhenUserDoesNotExist() throws Exception {
        UserRepo userRepo = mock(UserRepo.class);
        when(userRepo.getUserFromLogin("alice11")).thenReturn(null);

        LoginTask loginTask = new LoginTask(userRepo);
        Socket socket = mock(Socket.class);
        BufferedReader in = new BufferedReader(new StringReader(""));
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer, true);
        RegexGrammar response = new RegexGrammar("USER alice11\r\n");

        assertThrows(AuthentificationFailed.class, () ->
                loginTask.authenticate(socket, in, out, response, "randomchars"));
    }

    @Test
    void bytesToHexConvertsByteArrayToLowercaseHex() {
        LoginTask loginTask = new LoginTask(mock(UserRepo.class));

        String value = loginTask.bytesToHex(new byte[]{0x0A, 0x1F});

        assertEquals("0a1f", value);
    }
}
