package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.NotificationType;

public interface INotificationService {
    /**
     * Kullanıcıya anlık bildirim (Socket & FCM) gönderir.
     * Veritabanına kalıcı kayıt yapmaz, sadece anlık "sinyal" iletir.
     */
    Result sendNotification(Long userId, String title, String content, NotificationType type);
}
