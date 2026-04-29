package com.mefy.platemate.api.socket.abstracts;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.mefy.platemate.entities.dto.request.LocationUpdateRequest;

/**
 * Lokasyon domain'ine ait socket event'lerinin sözleşmesi.
 * update_location event'ini tanımlar.
 */
public interface ILocationSocketHandler extends ISocketRegistrar {
    void handleLocationUpdate(SocketIOClient client, LocationUpdateRequest data, AckRequest ackSender);
}
