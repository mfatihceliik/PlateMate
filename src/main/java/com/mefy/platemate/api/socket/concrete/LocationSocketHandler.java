package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.abstracts.ILocationSocketHandler;
import com.mefy.platemate.business.abstracts.IUserLocationService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LocationSocketHandler implements ILocationSocketHandler {

    private final IUserLocationService userLocationService;

    @Override
    public void registerEvents(SocketIOServer server) {
        server.addEventListener("update_location", LocationUpdateRequest.class, this::handleLocationUpdate);
    }

    @Override
    public void handleLocationUpdate(SocketIOClient client, LocationUpdateRequest data, AckRequest ackSender) {
        Long userId = client.get("userId");
        if (userId == null) {
            log.warn("Unauthenticated location update attempt from session {}", client.getSessionId());
            return;
        }

        log.info("Location update received from userId {}: {}, {}", userId, data.getLatitude(), data.getLongitude());

        Result result = userLocationService.updateLocation(userId, data.getLatitude(), data.getLongitude());

        if (result.isSuccess()) {
            log.info("Location updated successfully for userId {}", userId);
        } else {
            log.error("Failed to update location for userId {}: {}", userId, result.getMessage());
        }
    }
}
