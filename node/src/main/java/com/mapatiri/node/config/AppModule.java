package com.mapatiri.node.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.mapatiri.node.controller.Dispatcher;
import com.mapatiri.node.controller.TaskDispatcher;
import com.mapatiri.node.controller.ClientSessionEventNotifier;
import com.mapatiri.node.factory.SocketFactory;
import com.mapatiri.node.factory.UDPFactory;
import com.mapatiri.node.model.HeartbeatClock;
import com.mapatiri.node.repository.*;
import com.mapatiri.node.server.ClientNodeServer;
import com.mapatiri.node.service.*;
import com.mapatiri.node.task.LoginTask;
import com.mapatiri.node.task.RegisterTask;
import com.mapatiri.node.task.TaskConverter;

/**
 * Module de configuration pour le gestionnaire d'injection de dépendances.
 * Permet de configurer des singletons, transients et scoped.
 *
 * @author Timëo
 */
public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind interface to implementation
        bind(Dispatcher.class).to(TaskDispatcher.class).in(Singleton.class);
        bind(SpeakerBridgeService.class).to(SpeakerBridge.class).in(Singleton.class);

        // Json repo
        bind(ConfigRepository.class).to(JsonConfigRepository.class).in(Singleton.class);
        bind(UserRepo.class).to(JsonUserRepository.class).in(Singleton.class);
        bind(TagRepo.class).to(JsonTagRepository.class).in(Singleton.class);

        // Define singleton services
        bind(ConnectionStore.class).asEagerSingleton();
        bind(IdentityUtils.class).in(Singleton.class);

        bind(SocketFactory.class).in(Singleton.class);
        bind(ClientNodeServer.class).in(Singleton.class);
        bind(AuthTaskManager.class).in(Singleton.class);

        // auth task
        bind(ClientSessionEventNotifier.class).in(Singleton.class);
        bind(LoginTask.class).in(Singleton.class);
        bind(RegisterTask.class).in(Singleton.class);

        // Clock
        bind(HeartbeatClock.class).in(Singleton.class);
        bind(UDPFactory.class).in(Singleton.class);

        // Bridge and announcer
        bind(SpeakerAnnouncer.class).in(Singleton.class);
        bind(AESEncrypter.class).in(Singleton.class);
        bind(TaskConverter.class).in(Singleton.class);
    }

}
