package com.mapatiri.node.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.node.controller.Dispatcher;
import com.mapatiri.node.controller.TaskDispatcher;
import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.factory.SocketFactory;
import com.mapatiri.node.repository.*;
import com.mapatiri.node.server.ClientNodeServer;
import com.mapatiri.node.service.*;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;
import com.mapatiri.node.task.RequestTaskManager;
import com.mapatiri.node.task.TaskRepeater;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind interface to implementation
        bind(Dispatcher.class).to(TaskDispatcher.class).in(Singleton.class);

        // Json repo
        bind(ConfigRepository.class).to(JsonConfigRepository.class).in(Singleton.class);
        bind(UserRepo.class).to(UserRepository.class).in(Singleton.class);
        bind(TagRepo.class).to(TagRepository.class).in(Singleton.class);

        // Define singleton services
        bind(ConnectionStore.class).asEagerSingleton();

        bind(SocketFactory.class).in(Singleton.class);
        bind(ClientNodeServer.class).in(Singleton.class);
        bind(AuthTaskManager.class).in(Singleton.class);

        // auth task
        bind(ClientSessionEventNotifier.class).in(Singleton.class);
        bind(LoginTask.class).in(Singleton.class);
        bind(RegisterTask.class).in(Singleton.class);

        //Task thread
        bind(TaskRepeater.class).in(Singleton.class);
        bind(RequestTaskManager.class).in(Singleton.class);
        bind(TaskDispatcher.class).in(Singleton.class);

    }

}
