package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.abstracts.ILocationSocketHandler;
import com.mefy.platemate.api.socket.utilities.constants.SocketEvents;
import com.mefy.platemate.business.abstracts.IUserLocationService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.request.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class LocationSocketHandler implements ILocationSocketHandler {

    private final IUserLocationService userLocationService;
    private final SocketIOServer server;

    @Override
    public void registerEvents(SocketIOServer server) {
        server.addEventListener(SocketEvents.UPDATE_LOCATION, LocationUpdateRequest.class, this::handleLocationUpdate);
    }

    @Override
    public void handleLocationUpdate(SocketIOClient client, LocationUpdateRequest data, AckRequest ackSender) {
        Long userId = client.get("userId");
        if (userId == null) return;

        // Service returns the list of user IDs who should be notified (Business logic inside Service)
        DataResult<List<Long>> result = userLocationService.updateLocation(userId, data.getLatitude(), data.getLongitude());

        if (result.isSuccess()) {
            broadcastToRecipients(userId, data, result.getData());
        }
    }

    private void broadcastToRecipients(Long senderId, LocationUpdateRequest data, List<Long> recipients) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", senderId);
        payload.put("latitude", data.getLatitude());
        payload.put("longitude", data.getLongitude());

        for (Long recipientId : recipients) {
            server.getRoomOperations(SocketEvents.USER_ROOM_PREFIX + recipientId)
                    .sendEvent(SocketEvents.FRIEND_LOCATION_UPDATE, payload);
        }
    }
}
