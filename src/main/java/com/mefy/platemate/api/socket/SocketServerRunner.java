package com.mefy.platemate.api.socket;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketServerRunner implements CommandLineRunner {

    private final SocketIOServer server;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Socket.io server on port: {}", server.getConfiguration().getPort());
        server.start();
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping Socket.io server...");
        server.stop();
    }
}
