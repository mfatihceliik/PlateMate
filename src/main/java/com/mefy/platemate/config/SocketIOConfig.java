package com.mefy.platemate.config;

import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
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

        // netty-socketio'nun varsayılan JSON mapper'ı Java 8 tarih tiplerini bilmez; LocalDateTime
        // taşıyan event'ler (new_message → ChatMessageDto) serileştirmede patlayıp sessizce düşerdi.
        config.setJsonSupport(new SocketJacksonJsonSupport());

        // Handshake Auth Interceptor
        config.setAuthorizationListener(data -> {
            String token = data.getSingleUrlParam("token");
            if (token == null || token.isEmpty()) {
                log.warn("Socket handshake rejected: missing token param");
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }
            boolean isValid = tokenProvider.validateToken(token);
            if (!isValid) {
                log.warn("Socket handshake rejected: invalid token");
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }
            return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
        });

        log.info("Socket.IO server configured on {}:{}", host, port);
        return new SocketIOServer(config);
    }
}
