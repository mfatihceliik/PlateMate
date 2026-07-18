package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.abstracts.IChatSocketHandler;
import com.mefy.platemate.business.utilities.constants.SocketEvents;
import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
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
            // sendMessage persists and broadcasts new_message to every OTHER participant's user
            // room; the sender learns about their own message via the ack below, not the broadcast.
            result = chatMessageService.sendMessage(data, senderId);

            // Ack fires immediately on the {success, message, data} DataResult shape — the same
            // envelope every REST endpoint already returns — so Android has one consistent shape
            // to parse for success, business error, and thrown-exception cases. Sent whether or
            // not it succeeded so the client's 30s timeout never needs to fire on a message that
            // actually failed validation instantly.
            if (ackSender.isAckRequested()) {
                ackSender.sendAckData(result);
            }

            if (!result.isSuccess()) {
                // Business rule error (e.g., messaging disabled) — kept alongside the ack for any
                // listener that isn't the originating emit (e.g. future multi-tab/session support).
                client.sendEvent(SocketEvents.ERROR, result);
            }
        } catch (Exception e) {
            log.error("Socket error in handleSendMessage: {}", e.getMessage());
            // Hide raw exception message from client, log server-side
            DataResult<ChatMessageDto> errorResult = result != null
                    ? result
                    : new ErrorDataResult<>(messageService.getMessage(Messages.UNEXPECTED_ERROR));
            if (ackSender.isAckRequested()) {
                ackSender.sendAckData(errorResult);
            }
            client.sendEvent(SocketEvents.ERROR, errorResult);
        }
    }
}
