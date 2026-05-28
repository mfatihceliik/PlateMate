package com.mefy.platemate.entities.concrete;

import java.util.Set;

public final class FriendshipRequestStatusCodes {
    public static final Long REQUESTED_ID = 1L;
    public static final Long ACCEPTED_ID = 2L;
    public static final Long REJECTED_ID = 3L;
    public static final Long REMOVED_ID = 4L;

    public static final String REQUESTED_CODE = "REQUESTED";
    public static final String ACCEPTED_CODE = "ACCEPTED";
    public static final String REJECTED_CODE = "REJECTED";
    public static final String REMOVED_CODE = "REMOVED";

    public static final Set<Long> ACTIVE_IDS = Set.of(REQUESTED_ID, ACCEPTED_ID);

    private FriendshipRequestStatusCodes() {
    }

    public static String codeFromId(Long statusId) {
        if (statusId == null) return null;
        if (REQUESTED_ID.equals(statusId)) return REQUESTED_CODE;
        if (ACCEPTED_ID.equals(statusId)) return ACCEPTED_CODE;
        if (REJECTED_ID.equals(statusId)) return REJECTED_CODE;
        if (REMOVED_ID.equals(statusId)) return REMOVED_CODE;
        return null;
    }
}
