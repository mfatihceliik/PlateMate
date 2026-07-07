package com.mefy.platemate.business.utilities.constants;

public class ModerationConstants {

    private ModerationConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String REMOVED_BY_ACCEPTED_REPORT = "REMOVED_BY_ACCEPTED_REPORT";
    public static final String USER_REMOVED_REVIEW = "USER_REMOVED_REVIEW";
    public static final String APPROVED_BY_ADMIN = "APPROVED_BY_ADMIN";
    public static final String REJECTED_BY_ADMIN = "REJECTED_BY_ADMIN";
    public static final String REMOVED_BY_MODERATOR = "REMOVED_BY_MODERATOR";
    public static final String AUTO_HIDE_BY_REMOVAL_REQUEST = "AUTO_HIDE_BY_REMOVAL_REQUEST";
    public static final String USER_UPDATED_REVIEW = "USER_UPDATED_REVIEW";
    public static final String USER_RESUBMITTED_REJECTED_REVIEW = "USER_RESUBMITTED_REJECTED_REVIEW";
    public static final String USER_SUBMITTED_REVIEW = "USER_SUBMITTED_REVIEW";
    public static final String AUTO_PENDING_BY_REPORT_THRESHOLD = "AUTO_PENDING_BY_REPORT_THRESHOLD";
    public static final String NON_PREMIUM_TEXT_COMMENT_NOT_ALLOWED = "NON_PREMIUM_TEXT_COMMENT_NOT_ALLOWED";

}
