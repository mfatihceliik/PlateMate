package com.mefy.platemate.api.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SocketModule {

    private final JwtTokenProvider tokenProvider;

    public SocketModule(SocketIOServer server, 
                        List<ISocketEventHandler<?>> eventHandlers, 
                        JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;

        // Register Listeners
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());

        // Register Data Events Automatically (SOLID)
        for (ISocketEventHandler<?> handler : eventHandlers) {
            registerEvent(server, handler);
        }
    }

    private <T> void registerEvent(SocketIOServer server, ISocketEventHandler<T> handler) {
        server.addEventListener(handler.getEventName(), handler.getDataClass(), handler::onData);
    }

    private ConnectListener onConnected() {
        return client -> {
            String token = client.getHandshakeData().getSingleUrlParam("token");
            if (token != null) {
                try {
                    Long userId = tokenProvider.getUserIdFromToken(token);
                    client.set("userId", userId);
                    log.info("Client connected: {} (UserId: {})", client.getSessionId(), userId);
                } catch (Exception e) {
                    log.error("Auth error on connection: {}", e.getMessage());
                    client.disconnect();
                }
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            log.info("Client disconnected: {}", client.getSessionId());
        };
    }
}
