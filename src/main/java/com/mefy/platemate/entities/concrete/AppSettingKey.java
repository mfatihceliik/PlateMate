package com.mefy.platemate.entities.concrete;

public enum AppSettingKey {
    NON_PREMIUM_PLATE_FOLLOW_LIMIT("platemate.follow.non-premium-plate-limit"),
    NON_PREMIUM_PLATE_ALARM_LIMIT("platemate.alarm.non-premium-plate-limit"),
    PRE_APPROVAL_MESSAGE_LIMIT("platemate.messaging.pre-approval-message-limit"),
    COMMENT_REPORT_THRESHOLD("moderation.comment-report-threshold");

    private final String key;

    AppSettingKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
