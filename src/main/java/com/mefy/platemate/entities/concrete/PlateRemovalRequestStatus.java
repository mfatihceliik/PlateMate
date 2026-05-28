package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum PlateRemovalRequestStatus {
    OPEN(1L, "OPEN"),
    IN_REVIEW(2L, "IN_REVIEW"),
    ACCEPTED(3L, "ACCEPTED"),
    REJECTED(4L, "REJECTED");

    private final Long id;
    private final String code;

    PlateRemovalRequestStatus(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static PlateRemovalRequestStatus fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static PlateRemovalRequestStatus fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static PlateRemovalRequestStatus resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
