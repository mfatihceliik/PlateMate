package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;

import java.util.Arrays;

public enum UserSubscriptionStatus implements IEntity {
    PENDING(1L, "PENDING"),
    ACTIVE(2L, "ACTIVE"),
    EXPIRED(3L, "EXPIRED"),
    CANCELED(4L, "CANCELED");

    private final Long id;
    private final String code;

    UserSubscriptionStatus(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static UserSubscriptionStatus fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static UserSubscriptionStatus fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static UserSubscriptionStatus resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
