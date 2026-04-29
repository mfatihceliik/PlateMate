package com.mefy.platemate.business.concrete;

import com.corundumstudio.socketio.SocketIOServer;
import com.mefy.platemate.api.socket.utilities.constants.SocketEvents;
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
    private final SocketIOServer socketServer;

    @Override
    public Result sendNotification(Long userId, String title, String content, NotificationType type) {
        // 1. Kullanıcı tercihlerini kontrol et
        UserSettings settings = userSettingsDao.findByUserId(userId).orElse(null);
        if (settings != null) {
            if (type == NotificationType.MESSAGE && !settings.isMessageNotificationsEnabled()) {
                return new SuccessResult();
            }
            if (type == NotificationType.FRIEND_REQUEST && !settings.isFriendNotificationsEnabled()) {
                return new SuccessResult();
            }
        }

        // 2. Anlık Sinyal Hazırla (DTO)
        NotificationSignalDto signal = NotificationSignalDto.builder()
                .title(title)
                .content(content)
                .type(type)
                .timestamp(System.currentTimeMillis())
                .build();

        // 3. SOCKET PUSH (Uygulama açıksa)
        socketServer.getRoomOperations(SocketEvents.USER_ROOM_PREFIX + userId)
                .sendEvent(SocketEvents.NOTIFICATION_RECEIVED, new SuccessDataResult<>(signal));

        // 4. FCM PUSH (Uygulama kapalıysa/arka plandaysa)
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
