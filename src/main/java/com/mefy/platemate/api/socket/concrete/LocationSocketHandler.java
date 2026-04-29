package com.mefy.platemate.api.socket.concrete;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.abstracts.ILocationSocketHandler;
import com.mefy.platemate.api.socket.utilities.constants.SocketEvents;
import com.mefy.platemate.business.abstracts.IUserLocationService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.UserLocationDto;
import com.mefy.platemate.entities.dto.request.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

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

        // Servis katmanında lokasyon güncellenir
        DataResult<UserLocationDto> updateResult = userLocationService.updateLocation(userId, data.getLatitude(), data.getLongitude());

        if (updateResult.isSuccess()) {
            // Bildirim gidecek arkadaşlar servis katmanından alınır (Business logic)
            DataResult<List<Long>> recipientsResult = userLocationService.getFriendsToNotify(userId);
            
            if (recipientsResult.isSuccess() && !recipientsResult.getData().isEmpty()) {
                broadcastToRecipients(updateResult, recipientsResult.getData());
            }
        } else {
            // Hata durumunda sadece gönderen kullanıcıya bildir
            client.sendEvent(SocketEvents.ERROR, updateResult);
        }
    }

    private void broadcastToRecipients(DataResult<UserLocationDto> locationResult, List<Long> recipients) {
        for (Long recipientId : recipients) {
            server.getRoomOperations(SocketEvents.USER_ROOM_PREFIX + recipientId)
                    .sendEvent(SocketEvents.FRIEND_LOCATION_UPDATE, locationResult);
        }
    }
}
