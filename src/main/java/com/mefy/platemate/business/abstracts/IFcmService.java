package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.entities.dto.NotificationSignalDto;

import java.util.List;

public interface IFcmService {
    void sendPushNotification(List<String> tokens, NotificationSignalDto signal);
}
