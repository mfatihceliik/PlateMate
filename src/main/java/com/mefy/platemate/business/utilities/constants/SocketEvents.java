package com.mefy.platemate.business.utilities.constants;

public final class SocketEvents {
    // Chat Events
    public static final String JOIN_ROOM = "join_room";
    public static final String SEND_MESSAGE = "send_message";
    public static final String NEW_MESSAGE = "new_message";
    public static final String MESSAGE_DELIVERED = "message_delivered";
    public static final String MESSAGE_READ = "message_read";

    // Presence Events
    public static final String PRESENCE_ONLINE = "presence_online";
    public static final String PRESENCE_OFFLINE = "presence_offline";

    // Room Prefixes
    public static final String USER_ROOM_PREFIX = "user_";

    // General Events
    public static final String ERROR = "error";
    public static final String NOTIFICATION_RECEIVED = "notification_received";

    private SocketEvents() {
    } // Prevent instantiation
}
