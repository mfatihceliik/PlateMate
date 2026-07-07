package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ISocketPushService;
import com.mefy.platemate.business.utilities.constants.SocketEvents;
import com.mefy.platemate.business.abstracts.IFcmService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IFcmTokenDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.dto.NotificationSignalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationManager implements INotificationService {

    private final IUserSettingsDao userSettingsDao;
    private final IFcmTokenDao fcmTokenDao;
    private final IFcmService fcmService;
    private final ISocketPushService socketPushService;

    @Override
    public Result sendNotification(Long userId, String title, String content, String typeName) {
        return sendNotification(userId, title, content, typeName, null);
    }

    @Override
    public Result sendNotification(Long userId, String title, String content, String typeName, Long referenceId) {
        NotificationType type = NotificationType.fromNameOrDefault(typeName, NotificationType.SYSTEM);

        // 1. Check user preferences
        UserSettings settings = userSettingsDao.findByUserId(userId).orElse(null);
        if (settings != null) {
            if (type == NotificationType.MESSAGE && !settings.isMessageNotificationsEnabled()) {
                return new SuccessResult();
            }
            if (type == NotificationType.FRIEND_REQUEST && !settings.isFriendNotificationsEnabled()) {
                return new SuccessResult();
            }
            if (type == NotificationType.PLATE_REVIEW && !settings.isPlateReviewNotificationsEnabled()) {
                return new SuccessResult();
            }
            if (type == NotificationType.NEW_FOLLOWER && !settings.isNewFollowerNotificationsEnabled()) {
                return new SuccessResult();
            }
        }

        // 2. Prepare instant signal (DTO)
        NotificationSignalDto signal = NotificationSignalDto.builder()
                .title(title)
                .content(content)
                .type(type)
                .timestamp(System.currentTimeMillis())
                .referenceId(referenceId)
                .build();

        // 3. SOCKET PUSH (If app is open) — emit raw signal DTO for consistent client parsing
        socketPushService.sendToUser(userId, SocketEvents.NOTIFICATION_RECEIVED, signal);

        // 4. FCM PUSH (If app is closed/in background)
        sendFcmNotification(userId, signal);

        return new SuccessResult();
    }

    private void sendFcmNotification(Long userId, NotificationSignalDto signal) {
        var tokens = fcmTokenDao.findByUserId(userId);
        if (tokens.isEmpty()) {
            return;
        }

        List<String> tokenList = tokens.stream()
                .map(t -> t.getToken())
                .collect(Collectors.toList());

        fcmService.sendPushNotification(tokenList, signal);
    }
}
