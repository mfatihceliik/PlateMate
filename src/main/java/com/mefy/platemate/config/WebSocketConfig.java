package com.mefy.platemate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP konfigürasyonu.
 *
 * Client bağlantı: ws://host:port/ws (SockJS fallback ile)
 * Subscribe: /topic/room/{roomId} (mesaj dinleme)
 * Send: /app/chat.send (mesaj gönderme)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client'ların subscribe edeceği prefix
        config.enableSimpleBroker("/topic");
        // Client'ların mesaj göndereceği prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket bağlantı endpoint'i
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // Mobil uygulama için tüm origin'lere izin ver
    }
}
