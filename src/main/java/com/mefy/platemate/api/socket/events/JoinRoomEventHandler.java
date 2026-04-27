package com.mefy.platemate.api.socket.events;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.mefy.platemate.api.socket.ISocketEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JoinRoomEventHandler implements ISocketEventHandler<String> {

    @Override
    public String getEventName() {
        return "join_room";
    }

    @Override
    public Class<String> getDataClass() {
        return String.class;
    }

    @Override
    public void onData(SocketIOClient client, String roomId, AckRequest ackSender) {
        client.joinRoom(roomId);
        log.info("Client {} joined room: {}", client.getSessionId(), roomId);
    }
}
