package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.abstracts.IChatSocketHandler;
import com.mefy.platemate.business.utilities.constants.SocketEvents;
import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.business.abstracts.IParticipantService;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.business.utilities.constants.Messages;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatSocketHandler implements IChatSocketHandler {

    private final IChatMessageService chatMessageService;
    private final IParticipantService participantService;
    private final IMessageService messageService;

    @Override
    public void registerEvents(SocketIOServer server) {
        server.addEventListener(SocketEvents.JOIN_ROOM, Long.class, this::handleJoinRoom);
        server.addEventListener(SocketEvents.SEND_MESSAGE, SendMessageRequest.class, this::handleSendMessage);
    }

    @Override
    public void handleJoinRoom(SocketIOClient client, Long roomId, AckRequest ackSender) {
        Long userId = client.get("userId");
        if (userId == null) return;

        if (participantService.isRoomMember(userId, roomId)) {
            client.joinRoom(roomId.toString());
            log.info("User {} joined room {}", userId, roomId);
        }
    }

    @Override
    public void handleSendMessage(SocketIOClient client, SendMessageRequest data, AckRequest ackSender) {
        Long senderId = client.get("userId");
        if (senderId == null) return;

        if (!participantService.isRoomMember(senderId, data.getChatRoomId())) {
            return;
        }

        DataResult<ChatMessageDto> result = null;
        try {
            // sendMessage persists and broadcasts new_message to each participant's user room
            // (sender included), so no room broadcast is needed here.
            result = chatMessageService.sendMessage(data, senderId);

            if (!result.isSuccess()) {
                // Business rule error (e.g., messaging disabled)
                client.sendEvent(SocketEvents.ERROR, result);
            }
        } catch (Exception e) {
            log.error("Socket error in handleSendMessage: {}", e.getMessage());
            // Hide raw exception message from client, log server-side
            client.sendEvent(SocketEvents.ERROR, result != null ? result : new ErrorResult(messageService.getMessage(Messages.UNEXPECTED_ERROR)));
        }
    }
}
