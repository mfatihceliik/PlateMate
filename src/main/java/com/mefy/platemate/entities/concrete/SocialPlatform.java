package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum SocialPlatform {
    INSTAGRAM(1L, "INSTAGRAM"),
    X(2L, "X"),
    SNAPCHAT(3L, "SNAPCHAT"),
    LINKEDIN(4L, "LINKEDIN"),
    FACEBOOK(5L, "FACEBOOK");

    private final Long id;
    private final String code;

    SocialPlatform(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static SocialPlatform fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static SocialPlatform fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static SocialPlatform resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
