package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum PlateReviewStatus {
    PENDING_REVIEW(1L, "PENDING_REVIEW"),
    APPROVED(2L, "APPROVED"),
    REJECTED(3L, "REJECTED"),
    REMOVED_BY_USER(4L, "REMOVED_BY_USER"),
    REMOVED_BY_MODERATOR(5L, "REMOVED_BY_MODERATOR"),
    REMOVED_BY_LEGAL_REQUEST(6L, "REMOVED_BY_LEGAL_REQUEST");

    private final Long id;
    private final String code;

    PlateReviewStatus(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static PlateReviewStatus fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static PlateReviewStatus fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
