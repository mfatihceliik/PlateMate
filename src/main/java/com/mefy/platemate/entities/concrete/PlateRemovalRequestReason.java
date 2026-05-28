package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum PlateRemovalRequestReason {
    PLATE_BELONGS_TO_ME(1L, "PLATE_BELONGS_TO_ME"),
    FALSE_INFORMATION(2L, "FALSE_INFORMATION"),
    PRIVACY_REQUEST(3L, "PRIVACY_REQUEST"),
    HARASSMENT(4L, "HARASSMENT"),
    LEGAL_REQUEST(5L, "LEGAL_REQUEST"),
    OTHER(6L, "OTHER");

    private final Long id;
    private final String code;

    PlateRemovalRequestReason(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static PlateRemovalRequestReason fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static PlateRemovalRequestReason fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static PlateRemovalRequestReason resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
