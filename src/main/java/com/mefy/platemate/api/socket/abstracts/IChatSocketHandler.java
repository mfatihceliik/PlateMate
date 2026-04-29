package com.mefy.platemate.api.socket.abstracts;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;

/**
 * Chat domain'ine ait tüm socket event'lerinin sözleşmesi.
 * join_room ve send_message event'lerini tanımlar.
 */
public interface IChatSocketHandler extends ISocketRegistrar {
    void handleJoinRoom(SocketIOClient client, Long roomId, AckRequest ackSender);
    void handleSendMessage(SocketIOClient client, SendMessageRequest data, AckRequest ackSender);
}
