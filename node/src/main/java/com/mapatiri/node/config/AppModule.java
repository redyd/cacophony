package com.mapatiri.node.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.node.controller.Dispatcher;
import com.mapatiri.node.controller.TaskDispatcher;
import com.mapatiri.node.factory.SocketFactory;
import com.mapatiri.node.repository.ConfigRepository;
import com.mapatiri.node.repository.JsonConfigRepository;
import com.mapatiri.node.server.NodeServer;
import com.mapatiri.node.service.*;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind interface to implementation
        bind(ConfigRepository.class).to(JsonConfigRepository.class).in(Singleton.class);
        bind(Dispatcher.class).to(TaskDispatcher.class).in(Singleton.class);

        // Define singleton services
        bind(ConnectionStore.class).in(Singleton.class);
        bind(SocketFactory.class).in(Singleton.class);
        bind(NodeServer.class).in(Singleton.class);
        bind(AuthTaskManager.class).in(Singleton.class);

        // auth task
        bind(LoginTask.class).in(Singleton.class);
        bind(RegisterTask.class).in(Singleton.class);
    }

}
