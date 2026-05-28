package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum PlateStatus {
    ACTIVE(1L, "ACTIVE"),
    PENDING(2L, "PENDING"),
    HIDDEN_BY_REQUEST(3L, "HIDDEN_BY_REQUEST"),
    BLOCKED(4L, "BLOCKED"),
    DELETED(5L, "DELETED");

    private final Long id;
    private final String code;

    PlateStatus(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static PlateStatus fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static PlateStatus fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static PlateStatus resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
