package com.mefy.platemate.api.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketModule {

    private final SocketIOServer server;
    private final IChatMessageService chatMessageService;
    private final JwtTokenProvider tokenProvider;

    public SocketModule(SocketIOServer server, IChatMessageService chatMessageService, JwtTokenProvider tokenProvider) {
        this.server = server;
        this.chatMessageService = chatMessageService;
        this.tokenProvider = tokenProvider;

        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("send_message", SendMessageRequest.class, onMessageReceived());
        server.addEventListener("join_room", String.class, onJoinRoom());
    }

    private ConnectListener onConnected() {
        return client -> {
            String token = client.getHandshakeData().getSingleUrlParam("token");
            if (token != null) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                client.set("userId", userId);
                log.info("Client connected: {} (UserId: {})", client.getSessionId(), userId);
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            log.info("Client disconnected: {}", client.getSessionId());
        };
    }

    private DataListener<String> onJoinRoom() {
        return (client, roomId, ackSender) -> {
            client.joinRoom(roomId);
            log.info("Client {} joined room: {}", client.getSessionId(), roomId);
        };
    }

    private DataListener<SendMessageRequest> onMessageReceived() {
        return (client, data, ackSender) -> {
            Long senderId = client.get("userId");
            if (senderId == null) return;

            log.info("Message received from {}: {}", senderId, data.getContent());

            User sender = new User();
            sender.setId(senderId);

            ChatRoom room = new ChatRoom();
            room.setId(data.getChatRoomId());

            ChatMessage message = new ChatMessage();
            message.setSender(sender);
            message.setChatRoom(room);
            message.setContent(data.getContent());

            // DB'ye kaydet
            var result = chatMessageService.sendMessage(message);

            if (result.isSuccess()) {
                // Odadaki herkese (gönderen dahil) mesajı yay
                server.getRoomOperations(data.getChatRoomId().toString())
                        .sendEvent("new_message", result.getData());
            }
        };
    }
}
