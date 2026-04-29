package com.mefy.platemate.business.concrete;

import com.google.firebase.messaging.*;
import com.mefy.platemate.business.abstracts.IFcmService;
import com.mefy.platemate.entities.dto.NotificationSignalDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FcmManager implements IFcmService {

    @Override
    public void sendPushNotification(List<String> tokens, NotificationSignalDto signal) {
        if (tokens == null || tokens.isEmpty()) return;

        Notification notification = Notification.builder()
                .setTitle(signal.getTitle())
                .setBody(signal.getContent())
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .putData("type", signal.getType().name())
                .putData("timestamp", String.valueOf(signal.getTimestamp()))
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Successfully sent {} FCM messages. Failures: {}", 
                    response.getSuccessCount(), response.getFailureCount());
            
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.warn("FCM delivery failed for token {}: {}", 
                                tokens.get(i), responses.get(i).getException().getMessage());
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Firebase messaging error: {}", e.getMessage());
        }
    }
}
