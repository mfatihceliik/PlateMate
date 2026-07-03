package com.mefy.platemate.api.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.mefy.platemate.api.socket.abstracts.ISocketRegistrar;
import com.mefy.platemate.business.utilities.constants.SocketEvents;
import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.business.abstracts.IParticipantService;
import com.mefy.platemate.business.abstracts.IPresenceService;
import com.mefy.platemate.business.abstracts.ISocketPushService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SocketModule {

    private final JwtTokenProvider tokenProvider;
    private final IParticipantService participantService;
    private final IChatMessageService chatMessageService;
    private final IPresenceService presenceService;
    private final ISocketPushService socketPushService;

    public SocketModule(
            SocketIOServer server,
            List<ISocketRegistrar> registrars,
            JwtTokenProvider tokenProvider,
            IParticipantService participantService,
            IChatMessageService chatMessageService,
            IPresenceService presenceService,
            ISocketPushService socketPushService
    ) {
        this.tokenProvider = tokenProvider;
        this.participantService = participantService;
        this.chatMessageService = chatMessageService;
        this.presenceService = presenceService;
        this.socketPushService = socketPushService;

        // Register lifecycle listeners
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());

        // Each handler registers its own events (SRP)
        for (ISocketRegistrar registrar : registrars) {
            registrar.registerEvents(server);
        }
    }

    private ConnectListener onConnected() {
        return client -> {
            String token = client.getHandshakeData().getSingleUrlParam("token");

            // Reject connection if token is missing or invalid
            if (token == null || token.isBlank()) {
                log.warn("Connection rejected — no token provided: {}", client.getSessionId());
                client.disconnect();
                return;
            }

            try {
                Long userId = tokenProvider.getUserIdFromToken(token);
                client.set("userId", userId);

                // Force user to join their own private room (For individual notifications)
                client.joinRoom(SocketEvents.USER_ROOM_PREFIX + userId);

                // Auto-join all rooms the user is a member of
                var participationResult = participantService.getByUserId(userId);
                if (participationResult.isSuccess()) {
                    participationResult.getData().forEach(roomId -> {
                        client.joinRoom(roomId.toString());
                    });
                }

                // Mark incoming messages delivered now that the recipient is online (double tick)
                try {
                    chatMessageService.markPendingDelivered(userId);
                } catch (Exception e) {
                    log.error("Failed to mark pending messages delivered for user {}: {}", userId, e.getMessage());
                }

                // Let chat partners know this user is now online (respects reciprocal visibility)
                try {
                    presenceService.broadcastPresence(userId, true);
                } catch (Exception e) {
                    log.error("Failed to broadcast online presence for user {}: {}", userId, e.getMessage());
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

            // Only mark offline when the user has no remaining connections (multi-device safe)
            if (userId != null && !socketPushService.isUserOnline(userId)) {
                try {
                    presenceService.broadcastPresence(userId, false);
                } catch (Exception e) {
                    log.error("Failed to broadcast offline presence for user {}: {}", userId, e.getMessage());
                }
            }
        };
    }
}
