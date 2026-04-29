package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.abstracts.IChatSocketHandler;
import com.mefy.platemate.api.socket.utilities.constants.SocketEvents;
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
        server.addEventListener(SocketEvents.JOIN_ROOM, Long.class, this::handleJoinRoom);
        server.addEventListener(SocketEvents.SEND_MESSAGE, SendMessageRequest.class, this::handleSendMessage);
    }

    @Override
    public void handleJoinRoom(SocketIOClient client, Long roomId, AckRequest ackSender) {
        Long userId = client.get("userId");
        if (userId == null) return;

        if (participantDao.existsByUserIdAndChatRoomId(userId, roomId)) {
            client.joinRoom(roomId.toString());
            log.info("User {} joined room {}", userId, roomId);
        }
    }

    @Override
    public void handleSendMessage(SocketIOClient client, SendMessageRequest data, AckRequest ackSender) {
        Long senderId = client.get("userId");
        if (senderId == null) return;

        if (!participantDao.existsByUserIdAndChatRoomId(senderId, data.getChatRoomId())) {
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

        try {
            var result = chatMessageService.sendMessage(message);

            if (result.isSuccess()) {
                client.getNamespace()
                        .getRoomOperations(data.getChatRoomId().toString())
                        .sendEvent(SocketEvents.NEW_MESSAGE, result.getData());
            }
        } catch (Exception e) {
            log.error("Socket error in handleSendMessage: {}", e.getMessage());
            // Client'a hata gönderilebilir (opsiyonel)
        }
    }
}
