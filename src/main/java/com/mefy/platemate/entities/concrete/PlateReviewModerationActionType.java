package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum PlateReviewModerationActionType {
    SUBMITTED_FOR_REVIEW(1L, "SUBMITTED_FOR_REVIEW"),
    APPROVED_BY_ADMIN(2L, "APPROVED_BY_ADMIN"),
    REJECTED_BY_ADMIN(3L, "REJECTED_BY_ADMIN"),
    REMOVED_BY_MODERATOR(4L, "REMOVED_BY_MODERATOR"),
    REMOVED_BY_USER(5L, "REMOVED_BY_USER"),
    AUTO_PENDING_BY_REPORT_THRESHOLD(6L, "AUTO_PENDING_BY_REPORT_THRESHOLD"),
    BACKFILL_SNAPSHOT(7L, "BACKFILL_SNAPSHOT");

    private final Long id;
    private final String code;

    PlateReviewModerationActionType(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static PlateReviewModerationActionType fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static PlateReviewModerationActionType fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static PlateReviewModerationActionType resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
