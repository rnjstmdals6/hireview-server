package com.example.hireviewserver.config;

import org.h2.tools.Server;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class H2ConsoleConfig {

    private Server webServer;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws SQLException {
        this.webServer = Server.createWebServer("-webPort", "8081", "-tcpAllowOthers").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        this.webServer.stop();
    }
}