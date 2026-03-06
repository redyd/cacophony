package com.mapatiri.node.exception;

public class ServerSocketInitializer extends Exception {
    public ServerSocketInitializer(Exception message) {
        super(message);
    }
    public ServerSocketInitializer(String message) {
        super(message);
    }
}
