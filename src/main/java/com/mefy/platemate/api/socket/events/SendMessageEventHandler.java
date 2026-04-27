package com.mefy.platemate.api.socket.events;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.mefy.platemate.api.socket.ISocketEventHandler;
import com.mefy.platemate.business.abstracts.IChatMessageService;
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
public class SendMessageEventHandler implements ISocketEventHandler<SendMessageRequest> {

    private final IChatMessageService chatMessageService;

    @Override
    public String getEventName() {
        return "send_message";
    }

    @Override
    public Class<SendMessageRequest> getDataClass() {
        return SendMessageRequest.class;
    }

    @Override
    public void onData(SocketIOClient client, SendMessageRequest data, AckRequest ackSender) {
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

        var result = chatMessageService.sendMessage(message);

        if (result.isSuccess()) {
            client.getNamespace()
                    .getRoomOperations(data.getChatRoomId().toString())
                    .sendEvent("new_message", result.getData());
        }
    }
}
