package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.abstracts.IChatSocketHandler;
import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.dataAccess.abstracts.IParticipantDao;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatSocketHandler implements IChatSocketHandler {

    private final IChatMessageService chatMessageService;
    private final IParticipantDao participantDao;

    @Override
    public void registerEvents(SocketIOServer server) {
        server.addEventListener("join_room", Long.class, this::handleJoinRoom);
        server.addEventListener("send_message", SendMessageRequest.class, this::handleSendMessage);
    }

    @Override
    public void handleJoinRoom(SocketIOClient client, Long roomId, AckRequest ackSender) {
        Long userId = client.get("userId");
        if (userId == null) {
            log.warn("Unauthenticated join_room attempt from session {}", client.getSessionId());
            return;
        }

        // Participant tablosunda kullanıcının bu odaya üye olup olmadığını kontrol et
        boolean isMember = participantDao.existsByUserIdAndChatRoomId(userId, roomId);
        if (!isMember) {
            log.warn("User {} attempted to join room {} without membership", userId, roomId);
            return;
        }

        client.joinRoom(roomId.toString());
        log.info("User {} joined room {}", userId, roomId);
    }

    @Override
    public void handleSendMessage(SocketIOClient client, SendMessageRequest data, AckRequest ackSender) {
        Long senderId = client.get("userId");
        if (senderId == null) {
            log.warn("Unauthenticated send_message attempt from session {}", client.getSessionId());
            return;
        }

        // Mesaj göndermeden önce oda üyeliği doğrula
        boolean isMember = participantDao.existsByUserIdAndChatRoomId(senderId, data.getChatRoomId());
        if (!isMember) {
            log.warn("User {} attempted to send message to room {} without membership", senderId, data.getChatRoomId());
            return;
        }

        User sender = new User();
        sender.setId(senderId);

        ChatRoom room = new ChatRoom();
        room.setId(data.getChatRoomId());

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setChatRoom(room);
        message.setContent(data.getContent());

        var result = chatMessageService.sendMessage(message);

        if (result.isSuccess()) {
            client.getNamespace()
                    .getRoomOperations(data.getChatRoomId().toString())
                    .sendEvent("new_message", result.getData());
        }
    }
}
