package com.mefy.platemate.api.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.mefy.platemate.api.socket.abstracts.ISocketRegistrar;
import com.mefy.platemate.business.abstracts.IParticipantService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SocketModule {

    private final JwtTokenProvider tokenProvider;
    private final IParticipantService participantService;

    public SocketModule(
            SocketIOServer server,
            List<ISocketRegistrar> registrars,
            JwtTokenProvider tokenProvider,
            IParticipantService participantService
    ) {
        this.tokenProvider = tokenProvider;
        this.participantService = participantService;

        // Lifecycle listener'ları kaydet
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());

        // Her handler kendi event'lerini kendisi register eder (SRP)
        for (ISocketRegistrar registrar : registrars) {
            registrar.registerEvents(server);
        }
    }

    private ConnectListener onConnected() {
        return client -> {
            String token = client.getHandshakeData().getSingleUrlParam("token");

            // Token yoksa veya geçersizse bağlantıyı reddet
            if (token == null || token.isBlank()) {
                log.warn("Connection rejected — no token provided: {}", client.getSessionId());
                client.disconnect();
                return;
            }

            try {
                Long userId = tokenProvider.getUserIdFromToken(token);
                client.set("userId", userId);

                // Kullanıcının üye olduğu tüm odalara otomatik katıl (Auto-Join)
                var participationResult = participantService.getByUserId(userId);
                if (participationResult.isSuccess()) {
                    participationResult.getData().forEach(p -> {
                        client.joinRoom(p.getChatRoom().getId().toString());
                    });
                }

                log.info("Client connected: {} (UserId: {})", client.getSessionId(), userId);
            } catch (Exception e) {
                log.error("Connection rejected — invalid token: {}", e.getMessage());
                client.disconnect();
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            Long userId = client.get("userId");
            log.info("Client disconnected: {} (UserId: {})", client.getSessionId(), userId);
        };
    }
}
