package com.mefy.platemate.api.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;

public interface ISocketEventHandler<T> {
    String getEventName();
    Class<T> getDataClass();
    void onData(SocketIOClient client, T data, AckRequest ackSender);
}
