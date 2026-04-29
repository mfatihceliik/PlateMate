package com.mefy.platemate.api.socket.utilities.constants;

public final class SocketEvents {
    // Chat Events
    public static final String JOIN_ROOM = "join_room";
    public static final String SEND_MESSAGE = "send_message";
    public static final String NEW_MESSAGE = "new_message";

    // Location Events
    public static final String UPDATE_LOCATION = "update_location";
    public static final String FRIEND_LOCATION_UPDATE = "friend_location_update";

    // Room Prefixes
    public static final String USER_ROOM_PREFIX = "user_";

    private SocketEvents() {
    } // Prevent instantiation
}
