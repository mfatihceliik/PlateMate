package com.mefy.platemate.entities.concrete;

public enum PlateReviewModerationActionType {
    SUBMITTED_FOR_REVIEW,
    APPROVED_BY_ADMIN,
    REJECTED_BY_ADMIN,
    REMOVED_BY_MODERATOR,
    REMOVED_BY_USER,
    AUTO_PENDING_BY_REPORT_THRESHOLD,
    BACKFILL_SNAPSHOT
}
