package com.mefy.platemate.business.concrete;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.mefy.platemate.business.abstracts.IFcmService;
import com.mefy.platemate.entities.dto.NotificationSignalDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class FcmManager implements IFcmService {

    @Override
    public void sendPushNotification(List<String> tokens, NotificationSignalDto signal) {
        if (tokens == null || tokens.isEmpty()) return;

        // Firebase may be unconfigured (missing/invalid serviceAccountKey.json → FirebaseApp not
        // initialized). Skip instead of letting getInstance() throw IllegalStateException into
        // the caller, which runs inside the message-send @Transactional and would roll it back.
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FCM skipped: FirebaseApp not initialized (check serviceAccountKey.json).");
            return;
        }

        // DATA-ONLY payload (no setNotification): the Android client's onMessageReceived runs even
        // when the app is in the background, so it can BOTH render its own type-based notification
        // AND cache the incoming message into Room. A notification-payload would be auto-displayed
        // by the system tray without ever invoking onMessageReceived in background, leaving the
        // local chat list stale until the next foreground sync.
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putData("title", signal.getTitle() != null ? signal.getTitle() : "")
                .putData("content", signal.getContent() != null ? signal.getContent() : "")
                .putData("type", signal.getType().name())
                .putData("timestamp", String.valueOf(signal.getTimestamp()))
                // HIGH priority so data messages wake the app promptly in Doze/background.
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build());

        if (signal.getReferenceId() != null) {
            builder.putData("referenceId", String.valueOf(signal.getReferenceId()));
        }

        MulticastMessage message = builder.build();

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
        } catch (Exception e) {
            // Catch broadly (FirebaseMessagingException, IllegalStateException, etc.) so a
            // delivery failure never propagates into the message-send transaction.
            log.error("Firebase messaging error: {}", e.getMessage());
        }
    }
}
