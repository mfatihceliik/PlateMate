package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum UserRoleCode {
    NORMAL(1L, "NORMAL"),
    PREMIUM(2L, "PREMIUM"),
    ADMIN(3L, "ADMIN");

    private final Long id;
    private final String code;

    UserRoleCode(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static UserRoleCode fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static UserRoleCode fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static UserRoleCode resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
