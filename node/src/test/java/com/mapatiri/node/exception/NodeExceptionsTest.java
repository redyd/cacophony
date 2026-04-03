package com.mapatiri.node.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeExceptionsTest {

    @Test
    void keepsMessageForCheckedAndRuntimeExceptions() {
        assertEquals("a", new AuthentificationFailed("a").getMessage());
        assertEquals("b", new BadRequestException("b").getMessage());
        assertEquals("c", new NodeConfigSaveException("c").getMessage());
        assertEquals("d", new NodeFileException("d").getMessage());
        assertEquals("e", new SocketCreation("e").getMessage());
        assertEquals("f", new TagConfigException("f").getMessage());
        assertEquals("g", new UserConfigException("g").getMessage());
        assertEquals("h", new UserConflictException("h").getMessage());
    }

    @Test
    void socketCreationCanWrapException() {
        Exception cause = new IllegalStateException("boom");
        SocketCreation socketCreation = new SocketCreation(cause);

        assertEquals(cause, socketCreation.getCause());
    }
}
