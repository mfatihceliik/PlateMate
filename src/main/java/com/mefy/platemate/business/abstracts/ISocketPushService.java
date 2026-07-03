package com.mefy.platemate.business.abstracts;

public interface ISocketPushService {
    void sendToUser(Long userId, String eventName, Object payload);

    boolean isUserOnline(Long userId);
}
