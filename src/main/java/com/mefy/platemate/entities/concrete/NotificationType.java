package com.mefy.platemate.entities.concrete;

public enum NotificationType {
    SYSTEM,
    MESSAGE,
    FRIEND_REQUEST,
    PLATE_REVIEW,
    NEW_FOLLOWER;

    public static NotificationType fromNameOrDefault(String name, NotificationType defaultType) {
        if (name == null) {
            return defaultType;
        }
        for (NotificationType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return defaultType;
    }
}
