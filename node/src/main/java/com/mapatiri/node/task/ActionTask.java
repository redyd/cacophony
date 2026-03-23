package com.mapatiri.node.task;

public interface ActionTask extends Runnable {
    default void run() {

    }
    void executeTask();
    boolean shouldForward();
}
