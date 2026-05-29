package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.business.abstracts.ISocketPushService;
import com.mefy.platemate.business.utilities.constants.SocketEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketPushManager implements ISocketPushService {

    private final SocketIOServer socketServer;

    @Override
    public void sendToUser(Long userId, String eventName, Object payload) {
        socketServer.getRoomOperations(SocketEvents.USER_ROOM_PREFIX + userId)
                .sendEvent(eventName, payload);
    }
}
