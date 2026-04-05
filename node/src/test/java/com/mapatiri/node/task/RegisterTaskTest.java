package com.mapatiri.node.task;

import com.mapatiri.grammar.RegexGrammar;
import com.mapatiri.grammar.parse.Onboarding;
import com.mapatiri.grammar.parse.UserAction;
import com.mapatiri.node.exception.AuthentificationFailed;
import com.mapatiri.node.exception.UserConflictException;
import com.mapatiri.node.model.BcryptParams;
import com.mapatiri.node.repository.UserRepo;
import com.mapatiri.node.service.SimpleBcryptService;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterTaskTest {

    @Test
    void authenticateThrowsForInvalidResponseType() throws Exception {
        UserRepo userRepo = mock(UserRepo.class);
        SimpleBcryptService bcryptService = mock(SimpleBcryptService.class);
        RegisterTask registerTask = new RegisterTask(userRepo, bcryptService);

        Socket socket = mock(Socket.class);
        BufferedReader in = new BufferedReader(new StringReader(""));
        PrintWriter out = new PrintWriter(new StringWriter(), true);

        assertThrows(AuthentificationFailed.class, () ->
                registerTask.authenticate(socket, in, out, new UserAction("alice11"), "random"));
    }

    @Test
    void authenticateThrowsWhenUserAlreadyExists() throws Exception {
        UserRepo userRepo = mock(UserRepo.class);
        SimpleBcryptService bcryptService = mock(SimpleBcryptService.class);
        RegisterTask registerTask = new RegisterTask(userRepo, bcryptService);

        when(bcryptService.parseByCrypt(any(), anyInt())).thenReturn(new BcryptParams(10, "hash", "salt"));
        doThrow(new UserConflictException("exists")).when(userRepo).addUser(any());

        Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(new java.io.ByteArrayOutputStream());
        BufferedReader in = new BufferedReader(new StringReader(""));
        PrintWriter out = new PrintWriter(new StringWriter(), true);

        Onboarding onboarding = new Onboarding("alice11", "10", "$2b$10$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        assertThrows(AuthentificationFailed.class, () ->
                registerTask.authenticate(socket, in, out, onboarding, "random"));
    }
}
