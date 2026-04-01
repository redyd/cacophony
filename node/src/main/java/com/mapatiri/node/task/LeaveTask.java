package com.mapatiri.node.task;

import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.service.ConnectionStore;

public class LeaveTask implements ActionTask{

    private final ClientSessionEventNotifier notifier;
    private final ConnectionStore store;
    private final String login;

    public LeaveTask(ClientSessionEventNotifier notifier, ConnectionStore store, String login) {
        this.notifier = notifier;
        this.store = store;
        this.login = login;
    }

    @Override
    public void executeTask() {
        this.store.get(this.login).ifPresent(notifier::onUserLogout);
    }

    @Override
    public boolean shouldForward() {
        return false;
    }
}
