package com.mapatiri.node.exception;

public class SocketCreation extends Exception {
    public SocketCreation(Exception message) {
        super(message);
    }
    public SocketCreation(String message) {
        super(message);
    }
}
