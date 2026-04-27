package com.mefy.platemate.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Value("${socket.host}")
    private String host;

    @Value("${socket.port}")
    private int port;

    private final JwtTokenProvider tokenProvider;

    public SocketIOConfig(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);

        // Handshake Auth Interceptor
        config.setAuthorizationListener(data -> {
            // Token parameter veya Header'dan gelebilir. Socket.io client genelde query param kullanır.
            String token = data.getSingleUrlParam("token");
            if (token == null || token.isEmpty()) {
                return false;
            }
            return tokenProvider.validateToken(token);
        });

        return new SocketIOServer(config);
    }
}
