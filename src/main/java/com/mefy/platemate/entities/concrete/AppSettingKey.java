package com.mefy.platemate.entities.concrete;

public enum AppSettingKey {
    NON_PREMIUM_PLATE_FOLLOW_LIMIT("platemate.follow.non-premium-plate-limit", "nonPremiumPlateFollowLimit", 5),
    NON_PREMIUM_PLATE_ALARM_LIMIT("platemate.alarm.non-premium-plate-limit", "nonPremiumPlateAlarmLimit", 3),
    PRE_APPROVAL_MESSAGE_LIMIT("platemate.messaging.pre-approval-message-limit", "preApprovalMessageLimit", 3),
    COMMENT_REPORT_THRESHOLD("moderation.comment-report-threshold", "commentReportThreshold", 3);

    private final String key;
    private final String dtoFieldName;
    private final int defaultValue;

    AppSettingKey(String key, String dtoFieldName, int defaultValue) {
        this.key = key;
        this.dtoFieldName = dtoFieldName;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDtoFieldName() {
        return dtoFieldName;
    }

    public int getDefaultValue() {
        return defaultValue;
    }
}
